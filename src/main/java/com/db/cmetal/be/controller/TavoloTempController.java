package com.db.cmetal.be.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.service.TavoloTempService;
import com.db.cmetal.be.utils.AuthUtils;
import com.db.cmetal.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TavoloTempController {

	@Autowired
	AuthUtils authUtils;
	
    private final TavoloTempService tavoloTempService;

    @GetMapping("/api/tavoli/{tavoloId}/ordine-temporaneo")
    public Map<Long, Integer> getOrdineTemporaneo(@PathVariable Integer tavoloId) {
    	authUtils.getCurrentUserOrThrow(Constants.ROLE_CLIENT, Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
    	return tavoloTempService.getOrdineTemp(tavoloId);
    }
}
