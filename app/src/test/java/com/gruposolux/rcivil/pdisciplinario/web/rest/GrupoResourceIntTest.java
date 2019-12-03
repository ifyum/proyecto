//package com.gruposolux.rcivil.pdisciplinario.web.rest;
//
//import com.gruposolux.rcivil.pdisciplinario.AppApp;
//
//import com.gruposolux.rcivil.pdisciplinario.domain.Grupo;
//import com.gruposolux.rcivil.pdisciplinario.repository.GrupoRepository;
//import com.gruposolux.rcivil.pdisciplinario.service.dto.GrupoDTO;
//import com.gruposolux.rcivil.pdisciplinario.service.mapper.GrupoMapper;
//import com.gruposolux.rcivil.pdisciplinario.web.rest.errors.ExceptionTranslator;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.validation.Validator;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//
//import static com.gruposolux.rcivil.pdisciplinario.web.rest.TestUtil.createFormattingConversionService;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.hasItem;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Test class for the GrupoResource REST controller.
// *
// * @see GrupoResource
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = AppApp.class)
//public class GrupoResourceIntTest {
//
//    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
//    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";
//
//    @Autowired
//    private GrupoRepository grupoRepository;
//
//    @Autowired
//    private GrupoMapper grupoMapper;
//
//    @Autowired
//    private MappingJackson2HttpMessageConverter jacksonMessageConverter;
//
//    @Autowired
//    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;
//
//    @Autowired
//    private ExceptionTranslator exceptionTranslator;
//
//    @Autowired
//    private EntityManager em;
//
//    @Qualifier("defaultValidator")
//    @Autowired
//    private Validator validator;
//
//    private MockMvc restGrupoMockMvc;
//
//    private Grupo grupo;
//
//    @Before
//    public void setup() {
//        MockitoAnnotations.initMocks(this);
//        final GrupoResource grupoResource = new GrupoResource(null);
//        this.restGrupoMockMvc = MockMvcBuilders.standaloneSetup(grupoResource)
//            .setCustomArgumentResolvers(pageableArgumentResolver)
//            .setControllerAdvice(exceptionTranslator)
//            .setConversionService(createFormattingConversionService())
//            .setMessageConverters(jacksonMessageConverter)
//            .setValidator(validator).build();
//    }
//
//    /**
//     * Create an entity for this test.
//     *
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static Grupo createEntity(EntityManager em) {
//        Grupo grupo = new Grupo();
//        grupo.setNombre(DEFAULT_NOMBRE);
//        return grupo;
//    }
//
//    @Before
//    public void initTest() {
//        grupo = createEntity(em);
//    }
//
//    @Test
//    @Transactional
//    public void createGrupo() throws Exception {
//        int databaseSizeBeforeCreate = grupoRepository.findAll().size();
//
//        // Create the Grupo
//        GrupoDTO grupoDTO = grupoMapper.toDto(grupo);
//        restGrupoMockMvc.perform(post("/api/grupos")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(grupoDTO)))
//            .andExpect(status().isCreated());
//
//        // Validate the Grupo in the database
//        List<Grupo> grupoList = grupoRepository.findAll();
//        assertThat(grupoList).hasSize(databaseSizeBeforeCreate + 1);
//        Grupo testGrupo = grupoList.get(grupoList.size() - 1);
//        assertThat(testGrupo.getNombre()).isEqualTo(DEFAULT_NOMBRE);
//    }
//
//    @Test
//    @Transactional
//    public void createGrupoWithExistingId() throws Exception {
//        int databaseSizeBeforeCreate = grupoRepository.findAll().size();
//
//        // Create the Grupo with an existing ID
//        grupo.setId(1L);
//        GrupoDTO grupoDTO = grupoMapper.toDto(grupo);
//
//        // An entity with an existing ID cannot be created, so this API call must fail
//        restGrupoMockMvc.perform(post("/api/grupos")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(grupoDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Grupo in the database
//        List<Grupo> grupoList = grupoRepository.findAll();
//        assertThat(grupoList).hasSize(databaseSizeBeforeCreate);
//    }
//
//    @Test
//    @Transactional
//    public void getAllGrupos() throws Exception {
//        // Initialize the database
//        grupoRepository.saveAndFlush(grupo);
//
//        // Get all the grupoList
//        restGrupoMockMvc.perform(get("/api/grupos?sort=id,desc"))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(grupo.getId().intValue())))
//            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())));
//    }
//
//    @Test
//    @Transactional
//    public void getGrupo() throws Exception {
//        // Initialize the database
//        grupoRepository.saveAndFlush(grupo);
//
//        // Get the grupo
//        restGrupoMockMvc.perform(get("/api/grupos/{id}", grupo.getId()))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//            .andExpect(jsonPath("$.id").value(grupo.getId().intValue()))
//            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE.toString()));
//    }
//
//    @Test
//    @Transactional
//    public void getNonExistingGrupo() throws Exception {
//        // Get the grupo
//        restGrupoMockMvc.perform(get("/api/grupos/{id}", Long.MAX_VALUE))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @Transactional
//    public void updateGrupo() throws Exception {
//        // Initialize the database
//        grupoRepository.saveAndFlush(grupo);
//
//        int databaseSizeBeforeUpdate = grupoRepository.findAll().size();
//
//        // Update the grupo
//        Grupo updatedGrupo = grupoRepository.findById(grupo.getId()).get();
//        // Disconnect from session so that the updates on updatedGrupo are not directly saved in db
//        em.detach(updatedGrupo);
//        updatedGrupo.setNombre(UPDATED_NOMBRE);
//        GrupoDTO grupoDTO = grupoMapper.toDto(updatedGrupo);
//
//        restGrupoMockMvc.perform(put("/api/grupos")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(grupoDTO)))
//            .andExpect(status().isOk());
//
//        // Validate the Grupo in the database
//        List<Grupo> grupoList = grupoRepository.findAll();
//        assertThat(grupoList).hasSize(databaseSizeBeforeUpdate);
//        Grupo testGrupo = grupoList.get(grupoList.size() - 1);
//        assertThat(testGrupo.getNombre()).isEqualTo(UPDATED_NOMBRE);
//    }
//
//    @Test
//    @Transactional
//    public void updateNonExistingGrupo() throws Exception {
//        int databaseSizeBeforeUpdate = grupoRepository.findAll().size();
//
//        // Create the Grupo
//        GrupoDTO grupoDTO = grupoMapper.toDto(grupo);
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restGrupoMockMvc.perform(put("/api/grupos")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(grupoDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Grupo in the database
//        List<Grupo> grupoList = grupoRepository.findAll();
//        assertThat(grupoList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    public void deleteGrupo() throws Exception {
//        // Initialize the database
//        grupoRepository.saveAndFlush(grupo);
//
//        int databaseSizeBeforeDelete = grupoRepository.findAll().size();
//
//        // Get the grupo
//        restGrupoMockMvc.perform(delete("/api/grupos/{id}", grupo.getId())
//            .accept(TestUtil.APPLICATION_JSON_UTF8))
//            .andExpect(status().isOk());
//
//        // Validate the database is empty
//        List<Grupo> grupoList = grupoRepository.findAll();
//        assertThat(grupoList).hasSize(databaseSizeBeforeDelete - 1);
//    }
//
//    @Test
//    @Transactional
//    public void equalsVerifier() throws Exception {
//        TestUtil.equalsVerifier(Grupo.class);
//        Grupo grupo1 = new Grupo();
//        grupo1.setId(1L);
//        Grupo grupo2 = new Grupo();
//        grupo2.setId(grupo1.getId());
//        assertThat(grupo1).isEqualTo(grupo2);
//        grupo2.setId(2L);
//        assertThat(grupo1).isNotEqualTo(grupo2);
//        grupo1.setId(null);
//        assertThat(grupo1).isNotEqualTo(grupo2);
//    }
//
//    @Test
//    @Transactional
//    public void dtoEqualsVerifier() throws Exception {
//        TestUtil.equalsVerifier(GrupoDTO.class);
//        GrupoDTO grupoDTO1 = new GrupoDTO();
//        grupoDTO1.setId(1L);
//        GrupoDTO grupoDTO2 = new GrupoDTO();
//        assertThat(grupoDTO1).isNotEqualTo(grupoDTO2);
//        grupoDTO2.setId(grupoDTO1.getId());
//        assertThat(grupoDTO1).isEqualTo(grupoDTO2);
//        grupoDTO2.setId(2L);
//        assertThat(grupoDTO1).isNotEqualTo(grupoDTO2);
//        grupoDTO1.setId(null);
//        assertThat(grupoDTO1).isNotEqualTo(grupoDTO2);
//    }
//
//    @Test
//    @Transactional
//    public void testEntityFromId() {
//        assertThat(grupoMapper.fromId(42L).getId()).isEqualTo(42);
//        assertThat(grupoMapper.fromId(null)).isNull();
//    }
//}
