package com.db.cmetal.be.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.entity.CostoProdotto;
import com.db.cmetal.be.service.CostoProdottoService;

@RestController
@RequestMapping("/api/costo-prodotto")
public class CostoProdottoController {

    @Autowired
    private CostoProdottoService costoProdottoService;

    @GetMapping
    public List<CostoProdotto> getAll() {
        return costoProdottoService.getAll();
    }

    @PostMapping
    public CostoProdotto saveOrUpdate(@RequestBody Map<String, Object> body) {
        Long prodottoId = Long.valueOf(body.get("prodottoId").toString());
        Double costo = Double.valueOf(body.get("costo").toString()); // sicuro per integer o float
        return costoProdottoService.saveOrUpdate(prodottoId, costo);
    }

}
