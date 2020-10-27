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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.PessoaRepository;
import com.example.algamoney.api.service.PessoaService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/pessoas")
@Api(value="Finances", description="List of people that should be referenced in transactions")
public class PessoaResource {
	
		
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private PessoaService pessoaService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	
	@ApiOperation(value = "View a list of people")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved list"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	}
	)
	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_PESSOA')and #oauth2.hasScope('read')")
	public List<Pessoa> listar(){
		
		return pessoaRepository.findAll();
	}
	
	@ApiOperation(value = "Insert a new person on the list")
	@ApiResponses(value = {
	        @ApiResponse(code = 201, message = "Successfully insert a person in the system"),
	        @ApiResponse(code = 400, message = "Your payload is probably not correct for the api request"),
	        @ApiResponse(code = 401, message = "You are not authorized to use this resource"),
	}
	)
	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA')and #oauth2.hasScope('write')")
	public ResponseEntity<Pessoa> criar(@Valid @RequestBody Pessoa pessoa, HttpServletResponse response ){
		
	
		Pessoa pessoaSalva = pessoaRepository.save(pessoa);
		
		publisher.publishEvent(new RecursoCriadoEvent(this, response, pessoaSalva.getCodigo()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(pessoaSalva);
	
	}
	
	@ApiOperation(value = "View a person of a specific code")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved category or the category doesn't exist"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	}
	)
	@GetMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_PESSOA')and #oauth2.hasScope('read')")
	public ResponseEntity<?> buscarPeloCodigo(@PathVariable Long codigo) {
		Optional<Pessoa> pessoas =  pessoaRepository.findById(codigo);
	 
	 return Optional.empty() != null ? ResponseEntity.ok(pessoas) : ResponseEntity.notFound().build();
	 
	}
	
	@ApiOperation(value = "Delete a person on the list")
	@ApiResponses(value = {
	        @ApiResponse(code = 204, message = "Successfully deleted a person data"),
	        @ApiResponse(code = 401, message = "You are not authorized to use this resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	}
	)
	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('ROLE_REMOVER_PESSOA')and #oauth2.hasScope('write')")
	public void remover(@PathVariable Long codigo) {
		pessoaRepository.deleteById(codigo);
	}
	
	@ApiOperation(value = "Update a person on the list")
	@ApiResponses(value = {
	        @ApiResponse(code = 201, message = "Successfully update a person data"),
	        @ApiResponse(code = 401, message = "You are not authorized to use this resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	}
	)
	@PutMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA')and #oauth2.hasScope('write')")
	public Pessoa atualizar(@PathVariable Long codigo, @Valid @RequestBody Pessoa pessoa){
		
		Pessoa pessoaSalva = pessoaService.atualizar(codigo, pessoa);
		
		return this.pessoaRepository.save(pessoaSalva);
		
	}
	
	@ApiOperation(value = "Update the active status of a person on the list")
	@ApiResponses(value = {
	        @ApiResponse(code = 201, message = "Successfully update a person data"),
	        @ApiResponse(code = 401, message = "You are not authorized to use this resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	}
	)
	@PutMapping("/{codigo}/ativo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA')and #oauth2.hasScope('write')")
	public void atualizarPropriedadeAtivo(@PathVariable Long codigo, @RequestBody Boolean ativo) {
		pessoaService.atualizarPropriedadeAtivo(codigo, ativo);
	}
	
}
