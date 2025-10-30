package com.db.cmetal.be.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import com.db.cmetal.be.dto.TavoloMessage;
import com.db.cmetal.be.dto.TavoloMessagePayload;
import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Prodotto;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.service.CucinaWebSocketService;
import com.db.cmetal.be.service.ImpostazioniService;
import com.db.cmetal.be.service.OrdineService;
import com.db.cmetal.be.service.ProdottoService;
import com.db.cmetal.be.service.SessioneService;
import com.db.cmetal.be.service.TavoloTempService;
import com.db.cmetal.be.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TavoloWebSocketController {

    private final TavoloTempService tavoloTempService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final OrdineService ordineService;
    private final SessioneService sessioneService;
    private final ProdottoService prodottoService;
    private final CucinaWebSocketService cucinaWebSocketService;
    private final ImpostazioniService impostazioniService;
    
    private final ConcurrentMap<Integer, Object> tavoloLocks = new ConcurrentHashMap<>();
    
    private Object getLockForTavolo(Integer tavoloId) {
        return tavoloLocks.computeIfAbsent(tavoloId, k -> new Object());
    }

    @MessageMapping("/tavolo")
    public void handleTavoloMessage(TavoloMessage msg, Principal principal) throws Exception {
    	Sessione sessione;
    	Integer tavoloId;
    	if (principal instanceof UsernamePasswordAuthenticationToken auth
    	        && auth.getPrincipal() instanceof UserDetails) {
    	    sessione = sessioneService.findAttivaById(msg.getSessioneId());
    	    tavoloId = sessione.getTavolo().getId();
    	} else {
        	Claims claims = extractClaims(principal);
            sessione = getSessioneAttiva(claims);
            tavoloId = sessione.getTavolo().getId();

    	}

        switch (msg.getTipoEvento()) {
            case Constants.MSG_ADD_ITEM_TEMP, Constants.MSG_REMOVE_ITEM_TEMP -> handleTempItem(msg, sessione, tavoloId);
            case Constants.MSG_ORDER_SENT -> handleOrderSent(sessione, tavoloId);
            case Constants.MSG_GET_STATUS -> sendStatus(sessione, tavoloId);
            default -> sendError(tavoloId, "Tipo messaggio non gestito: " + msg.getTipoEvento());
        }
    }

    private Claims extractClaims(Principal principal) throws IllegalAccessException {
        if (!(principal instanceof UsernamePasswordAuthenticationToken auth)
                || !(
                     (auth.getPrincipal() instanceof Claims claims
                         && Constants.ROLE_CLIENT.equals(claims.get(Constants.CLAIM_ROLE)))
                     || auth.getPrincipal() instanceof UserDetails  // <-- staff loggato passa
                   )) {
            throw new IllegalAccessException("Utente non autorizzato");
        }

        // Se è un client, ritorna i Claims
        if (auth.getPrincipal() instanceof Claims claims) {
            return claims;
        }

        // Altrimenti è staff loggato, ritorna null
        return null;
    }


    private Sessione getSessioneAttiva(Claims claims) {
        Long sessioneId = claims.get(Constants.CLAIM_SESSIONE_ID, Long.class);
        Sessione sessione = sessioneService.findById(sessioneId);
        if (sessione == null || !Constants.SESSION_STATE_ACTIVE.equals(sessione.getStato()))
            throw new IllegalStateException("Sessione non attiva");
        return sessione;
    }

    private void handleTempItem(TavoloMessage msg, Sessione sessione, Integer tavoloId) throws Exception {
        synchronized (getLockForTavolo(tavoloId)) {
            TavoloMessagePayload payload = objectMapper.readValue(msg.getPayload(), TavoloMessagePayload.class);
            Long prodottoId = payload.getProdottoId();
            int delta = msg.getTipoEvento().equals(Constants.MSG_REMOVE_ITEM_TEMP) ? -payload.getQuantita() : payload.getQuantita();
            Prodotto prodotto = prodottoService.findById(prodottoId);

            if (Boolean.TRUE.equals(sessione.getIsAyce()) && isProdottoNormale(prodotto)) {
                int totalePortate = tavoloTempService.getOrdineTemp(tavoloId).entrySet().stream()
                        .mapToInt(e -> isProdottoNormale(prodottoService.findById(e.getKey())) ? e.getValue() : 0).sum();
                int maxPortate = sessione.getNumeroPartecipanti() * impostazioniService.getIntValue("portate_per_persona", 5);

                if (totalePortate + delta > maxPortate) {
                    sendError(tavoloId, Constants.ERR_LIMIT_PORTATE);
                    return;
                }
            }

            if (Boolean.TRUE.equals(sessione.getIsAyce()) && prodotto != null
                    && Boolean.TRUE.equals(prodotto.getIsLimitedPartecipanti()) && delta > 0) {
                int restante = sessione.getNumeroPartecipanti() - ordineService.findBySessione(sessione).stream()
                        .filter(o -> prodottoId.equals(o.getProdotto().getId()))
                        .mapToInt(Ordine::getQuantita).sum()
                        - tavoloTempService.getOrdineTemp(tavoloId).getOrDefault(prodottoId, 0);

                if (restante <= 0) {
                    sendError(tavoloId, Constants.ERR_LIMIT_PRODOTTO);
                    return;
                }
            }

            if (delta > 0) tavoloTempService.addItem(tavoloId, prodottoId, delta);
            else tavoloTempService.removeItem(tavoloId, prodottoId, -delta);

            int nuovaQuantita = tavoloTempService.getOrdineTemp(tavoloId).getOrDefault(prodottoId, 0);
            sendUpdateTempDelta(tavoloId, prodottoId, nuovaQuantita);
        }
    }

    private void handleOrderSent(Sessione sessione, Integer tavoloId) throws Exception {
        synchronized(getLockForTavolo(tavoloId)) {
        	boolean isError = false;
        	boolean isWarning = false;
            boolean isAyce = Boolean.TRUE.equals(sessione.getIsAyce());
            LocalDateTime ultimoOrdine = sessione.getUltimoOrdineInviato();

            boolean isCucinaAttiva = impostazioniService.findByChiave("cucina_attiva")
                    .map(i -> !"0".equals(i.getValore()))
                    .orElse(true);

            boolean isCooldownActive = isAyce 
                    && ultimoOrdine != null
                    && java.time.Duration.between(ultimoOrdine, LocalDateTime.now()).toMinutes() < impostazioniService.getIntValue("tempo_cooldown", 15);


            Map<Long, Integer> ordineTemp = tavoloTempService.getOrdineTemp(tavoloId);
            if (ordineTemp.isEmpty()) {
                sendError(tavoloId, "Non ci sono prodotti da inviare.");
                isError = true;
                return;
            }

            List<Ordine> ordineDaSalvare = new ArrayList<>();
            boolean hasNormalProduct = false;

            for (Map.Entry<Long, Integer> e : ordineTemp.entrySet()) {
                Long prodottoId = e.getKey();
                Integer quantita = e.getValue();
                if (quantita == null || quantita <= 0) continue;

                Prodotto prodotto = prodottoService.findById(prodottoId);
                if (prodotto == null) continue;

                boolean isNormale = isProdottoNormale(prodotto);
                if (isNormale) hasNormalProduct = true;

                if (isNormale && (!isCucinaAttiva || isCooldownActive)) continue;

                Ordine ordine = new Ordine();
                ordine.setFlagConsegnato(false);
                ordine.setOrario(LocalDateTime.now());
                ordine.setProdotto(prodotto);
                ordine.setQuantita(quantita);
                ordine.setSessione(sessione);
                ordine.setTavolo(sessione.getTavolo());
                ordine.setStato(Constants.ORDINE_STATO_INVIATO);
                ordine.setPrezzoUnitario((isAyce && isNormale) ? 0 : prodotto.getPrezzo());
                ordineDaSalvare.add(ordine);
            }

            if (hasNormalProduct && ordineDaSalvare.isEmpty()) {
                String msg = !isCucinaAttiva ? "La cucina è chiusa, puoi inviare solo bevande/vini/dessert."
                        : "Devi attendere prima di inviare un nuovo ordine. Puoi inviare bevande/vini/dessert.";
                sendError(tavoloId, msg);
                isError = true;
                return;
            }

            ordineDaSalvare.forEach(ordineService::save);

            if (isAyce && ordineDaSalvare.stream().anyMatch(o -> isProdottoNormale(o.getProdotto()))) {
                sessione.setUltimoOrdineInviato(LocalDateTime.now());
                sessioneService.save(sessione);
            }

            // Rimuovi solo i prodotti che sono stati salvati
            for (Ordine ordine : ordineDaSalvare) {
                Long prodottoId = ordine.getProdotto().getId();
                int quantita = ordine.getQuantita();
                tavoloTempService.removeItem(tavoloId, prodottoId, quantita);
                sendUpdateTempDelta(tavoloId, prodottoId, 
                tavoloTempService.getOrdineTemp(tavoloId).getOrDefault(prodottoId, 0));
            }


            if (!ordineDaSalvare.isEmpty()) cucinaWebSocketService.notifyNewOrder(null);

            if (hasNormalProduct && ordineDaSalvare.stream().allMatch(o -> !isProdottoNormale(o.getProdotto()))) {
                sendWarning(tavoloId, "Sono stati inviati solamente i prodotti delle categorie bevande/vini/dessert. Il resto è ancora nel carrello.");
                isWarning = true;
            }

            sendStatus(sessione, tavoloId);
            
            if(!isError && !isWarning) {
            	 messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId, new TavoloMessage("SUCCESS", sessione.getId(), "Ordine inviato con successo"));
            }
           
        }
    }

    private void sendStatus(Sessione sessione, Integer tavoloId) throws Exception {
        Map<Long, Integer> ordineTemp = tavoloTempService.getOrdineTemp(tavoloId);
        Map<String, Object> payload = new HashMap<>();

        payload.put("ordine", ordineTemp);
        payload.put("lastOrder", sessione.getUltimoOrdineInviato()); // null ok
        payload.put("cooldownMinuti", impostazioniService.getIntValue("tempo_cooldown", 15));
        payload.put("maxPortatePerPersona", impostazioniService.getIntValue("portate_per_persona", 5));
        payload.put("numeroPartecipanti", sessione.getNumeroPartecipanti());
        payload.put("pranzoStartHour", impostazioniService.getIntValue("ora_inizio_pranzo", 2));
        payload.put("pranzoEndHour", impostazioniService.getIntValue("ora_inizio_cena", 16));
        payload.put("isAyce", sessione.getIsAyce());

        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId,
                new TavoloMessage(Constants.MSG_UPDATE_TEMP, sessione.getId(), objectMapper.writeValueAsString(payload)));
    }


    private void sendError(Integer tavoloId, String msg) {
        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId, new TavoloMessage(Constants.MSG_ERROR, null, msg));
    }
    
    private void sendWarning(Integer tavoloId, String msg) {
        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId, new TavoloMessage(Constants.MSG_WARNING, null, msg));
    }

    private boolean isProdottoNormale(Prodotto p) {
        return p != null && p.getCategoria() != null && p.getCategoria().getId() < 100;
    }
    
    private void sendUpdateTempDelta(Integer tavoloId, Long prodottoId, int quantita) throws Exception {
        Map<String, Object> payload = Map.of(
                "prodottoId", prodottoId,
                "quantita", quantita
        );
        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId,
                new TavoloMessage(Constants.MSG_UPDATE_TEMP_DELTA, null, objectMapper.writeValueAsString(payload)));
    }
    
    public void clearTavoloQueue(Integer tavoloId) {
        // Rimuovi ordini temporanei
        tavoloTempService.clearOrdine(tavoloId);

        // Notifica il client
        messagingTemplate.convertAndSend(
            "/topic/tavolo/" + tavoloId,
            new TavoloMessage(Constants.MSG_REFRESH, null, "Refresh forzato della pagina")
        );
    }
    
    public void sendRefresh(Integer tavoloId) {
        messagingTemplate.convertAndSend(
            "/topic/tavolo/" + tavoloId,
            new TavoloMessage(Constants.MSG_REFRESH, null, "Refresh forzato della pagina")
        );
    }


}
