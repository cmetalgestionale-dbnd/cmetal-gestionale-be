package com.db.cmetal.be.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.dto.CurrentUserDTO;
import com.db.cmetal.be.dto.OrdineDto;
import com.db.cmetal.be.service.ComandeService;
import com.db.cmetal.be.utils.AuthUtils;
import com.db.cmetal.be.utils.Constants;

@RestController
@RequestMapping("/api/private/comande")
public class ComandeController {

	@Autowired
    ComandeService comandeService;
	@Autowired
	AuthUtils authUtils;

	@GetMapping("/filtrate")
	public List<OrdineDto> getOrdiniFiltrati(@RequestParam(defaultValue = "true") boolean soloAssegnati, @RequestParam(defaultValue = "false") boolean nascondiConsegnati) {
		CurrentUserDTO currentUser = authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
	    return comandeService.getComandeFiltrate(currentUser.userId(), soloAssegnati, nascondiConsegnati);
	}

    @PutMapping("/consegna/{id}")
    public ResponseEntity<?> toggleConsegnato(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
        comandeService.toggleConsegnato(id, body);
        return ResponseEntity.ok().build();
    }

}
