package com.gruposolux.rcivil.pdisciplinario.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.gruposolux.rcivil.pdisciplinario.domain.Perfil;
import com.gruposolux.rcivil.pdisciplinario.security.AuthoritiesConstants;
import com.gruposolux.rcivil.pdisciplinario.service.PerfilService;
import com.gruposolux.rcivil.pdisciplinario.service.dto.FiltroPerfilDTO;
import com.gruposolux.rcivil.pdisciplinario.service.dto.PerfilDTO;
import com.gruposolux.rcivil.pdisciplinario.web.rest.errors.BadRequestAlertException;
import com.gruposolux.rcivil.pdisciplinario.web.rest.util.HeaderUtil;
import com.gruposolux.rcivil.pdisciplinario.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Perfil.
 */
@RestController
@RequestMapping("/api")
public class PerfilResource {

    private final Logger log = LoggerFactory.getLogger(PerfilResource.class);

    private static final String ENTITY_NAME = "perfil";

    private final PerfilService perfilService;

    public PerfilResource(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * POST  /perfils : Create a new perfil.
     *
     * @param perfilDTO the perfilDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new perfilDTO, or with status 400 (Bad Request) if the perfil has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/perfils")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.CREAR_PERFIL + "\")")
    public ResponseEntity<PerfilDTO> createPerfil(@Valid @RequestBody PerfilDTO perfilDTO) throws URISyntaxException {
        log.debug("REST request to save Perfil : {}", perfilDTO);

        if (perfilDTO.getId() != null) {
            throw new BadRequestAlertException("A new perfil cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PerfilDTO result = perfilService.save(perfilDTO);
        return ResponseEntity.created(new URI("/api/perfils/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /perfils : Updates an existing perfil.
     *
     * @param perfilDTO the perfilDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated perfilDTO,
     * or with status 400 (Bad Request) if the perfilDTO is not valid,
     * or with status 500 (Internal Server Error) if the perfilDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/perfils")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.EDITAR_PERFIL + "\")")
    public ResponseEntity<PerfilDTO> updatePerfil(@Valid @RequestBody PerfilDTO perfilDTO) throws URISyntaxException {
        log.debug("REST request to update Perfil : {}", perfilDTO);
        if (perfilDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        PerfilDTO result = perfilService.save(perfilDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, perfilDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /perfils : get all the perfils.
     *
     * @param pageable the pagination information
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many)
     * @return the ResponseEntity with status 200 (OK) and the list of perfils in body
     */
    @GetMapping("/perfils")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.VISUALIZAR_PERFIL + "\")")
    public ResponseEntity<List<PerfilDTO>> getAllPerfils(Pageable pageable, @RequestParam(required = false, defaultValue = "false") boolean eagerload) {
        log.debug("REST request to get a page of Perfils");
        Page<PerfilDTO> page;


        if (eagerload) {
            page = perfilService.findAllWithEagerRelationships(pageable);
        } else {
            page = perfilService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, String.format("/api/perfils?eagerload=%b", eagerload));
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /perfiles : get all the perfiles.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of perfiles in body
     */
    @GetMapping("/perfils/paged")
    @Timed
    public ResponseEntity<List<Perfil>> getAllPerfilFilter(Pageable pageable, String nombre)
    {
        FiltroPerfilDTO filtroPerfilDTO = new FiltroPerfilDTO();
        filtroPerfilDTO.setNombre(nombre.equalsIgnoreCase("null") ||
            nombre.equalsIgnoreCase("TODOS") ? null : nombre);

        Page<Perfil> page = perfilService.findAllPerfil(pageable, filtroPerfilDTO);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/perfils");

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * GET  /perfils/:id : get the "id" perfil.
     *
     * @param id the id of the perfilDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the perfilDTO, or with status 404 (Not Found)
     */
    @GetMapping("/perfils/{id}")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.VISUALIZAR_PERFIL + "\")")
    public ResponseEntity<PerfilDTO> getPerfil(@PathVariable Long id) {
        log.debug("REST request to get Perfil : {}", id);
        Optional<PerfilDTO> perfilDTO = perfilService.findOne(id);
        return ResponseUtil.wrapOrNotFound(perfilDTO);
    }

    /**
     * DELETE  /perfils/:id : delete the "id" perfil.
     *
     * @param id the id of the perfilDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/perfils/{id}")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.ELIMINAR_PERFIL + "\")")
    public ResponseEntity<Void> deletePerfil(@PathVariable Long id) {
        log.debug("REST request to delete Perfil : {}", id);
        perfilService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * @return a string list of the all of the roles
     */
    @GetMapping("/perfil/authorities")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.CREAR_PERFIL + "\")")
    public List<String> getAuthorities() {
        return perfilService.getAuthorities();
    }
}
