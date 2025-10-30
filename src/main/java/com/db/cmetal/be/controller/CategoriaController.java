package com.db.cmetal.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.entity.Categoria;
import com.db.cmetal.be.service.CategoriaService;
import com.db.cmetal.be.utils.AuthUtils;
import com.db.cmetal.be.utils.Constants;

@RestController
@RequestMapping("/api/categorie")
public class CategoriaController {

	@Autowired
	CategoriaService categoriaService;

	@Autowired
	AuthUtils authUtils;

	@GetMapping
	public List<Categoria> getAllCategorie() {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_CLIENT, Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		return categoriaService.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Categoria> getCategoriaById(@PathVariable Long id) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_CLIENT, Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		Categoria categoria = categoriaService.findById(id);
		if (categoria != null) {
			return ResponseEntity.ok(categoria);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	public Categoria createCategoria(@RequestBody Categoria categoria) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_ADMIN);
		return categoriaService.save(categoria);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Categoria> updateCategoria(@PathVariable Long id, @RequestBody Categoria updatedCategoria) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_ADMIN);
		Categoria categoria = categoriaService.findById(id);
		if (categoria != null) {
			categoria.setNome(updatedCategoria.getNome());
			return ResponseEntity.ok(categoriaService.save(categoria));
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
	    authUtils.getCurrentUserOrThrow(Constants.ROLE_ADMIN);
	    Categoria categoria = categoriaService.findById(id);
	    if (categoria != null) {
	        categoriaService.softDelete(id);  // cancellazione logica
	        return ResponseEntity.noContent().build();
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}

}
