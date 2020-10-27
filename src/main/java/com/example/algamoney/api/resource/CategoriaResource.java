package com.example.algamoney.api.resource;


import java.util.List;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Categoria;
import com.example.algamoney.api.repository.CategoriaRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

interface category extends CategoriaRepository{
	public static final Integer codigo = null;
	public static final String nome = "";
}

@RestController
@RequestMapping("/categorias")
@Api(value="Finances", description="Category of transactions that should be registered")
public class CategoriaResource {
	
	@Autowired
	private CategoriaRepository categoriaRepository;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@ApiOperation(value = "View a list of categories")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved list"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	}
	)
	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA') and #oauth2.hasScope('read')")
	public List<Categoria> listar(){
		
		return categoriaRepository.findAll();
	}
	
	@ApiOperation(value = "Insert a new category on the list")
	@ApiResponses(value = {
	        @ApiResponse(code = 201, message = "Successfully created a category"),
	        @ApiResponse(code = 400, message = "Your payload is probably not correct for the api request"),
	        @ApiResponse(code = 401, message = "You are not authorized to use this resource"),
	}
	)
	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_CATEGORIA') and #oauth2.hasScope('write')")
	public ResponseEntity<Categoria> criar(@Valid @RequestBody Categoria categoria, HttpServletResponse response) {
		Categoria categoriaSalva = categoriaRepository.save(categoria);
		
		publisher.publishEvent(new RecursoCriadoEvent(this, response, categoriaSalva.getCodigo()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
		
	}
	

	@ApiOperation(value = "View the category of a specific code")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved category or the category doesn't exist"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	}
	)
	@GetMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA')and #oauth2.hasScope('read')")
	public ResponseEntity<?> buscarPeloCodigo(@PathVariable Long codigo) {
		 Optional<Categoria> categorias =  categoriaRepository.findById(codigo);
		 
		 return Optional.empty() != null ? ResponseEntity.ok(categorias) : ResponseEntity.notFound().build();
	}
//	@GetMapping("/{codigo}")
//	public ResponseEntity<category> buscarPeloCodigo(@PathVariable Long codigo) {
//		 Optional<Categoria> categorias =  categoriaRepository.findById(codigo);
//		 
//		 return Optional.empty() != null ? (ResponseEntity<category>) ((category) ResponseEntity.ok(categorias)) : ResponseEntity.notFound().build();
//	}

}
