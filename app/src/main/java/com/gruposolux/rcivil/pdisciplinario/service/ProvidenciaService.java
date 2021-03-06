package com.gruposolux.rcivil.pdisciplinario.service;

import com.gruposolux.rcivil.pdisciplinario.domain.*;
import com.gruposolux.rcivil.pdisciplinario.domain.enumeration.*;
import com.gruposolux.rcivil.pdisciplinario.repository.*;
import com.gruposolux.rcivil.pdisciplinario.service.dto.*;
import com.gruposolux.rcivil.pdisciplinario.service.mapper.*;
import com.gruposolux.rcivil.pdisciplinario.storage.AlfrescoStorageService;
//import com.sun.org.apache.bcel.internal.generic.SWITCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Providencia.
 */
@Service
@Transactional
public class ProvidenciaService {

    private final Logger log = LoggerFactory.getLogger(ProvidenciaService.class);
    private final ProvidenciaRepository providenciaRepository;
    private final ProvidenciaMapper providenciaMapper;
    private final AdjuntoService adjuntoService;
    private final AdjuntoMapper adjuntoMapper;
    private final ApplicationEventPublisher publisher;
    private final UserService userService;
    private final DerivacionRepository derivacionRepository;
    private final NotificacionInBrowserRepository notificacionInBrowserRepository;
    private final GrupoService grupoService;
    private final GrupoMapper grupoMapper;
    private final DocumentoMapper documentoMapper;
    private final DocumentoService documentoService;
    private final MovimientoProvidenciaService movimientoProvidenciaService;
    private final PlantillaService plantillaService;
    private final EntidadService entidadService;
    private final RespuestaService respuestaService;
    private final EntidadMapper entidadMapper;
    private final InvestigacionSumariaService investigacionSumariaService;
    private final SumarioAdministrativoService sumarioAdministrativoService;
    private final InvestigacionSumariaMapper investigacionSumariaMapper;
    private final SumarioAdministrativoMapper sumarioAdministrativoMapper;
    private final ProvidenciaStateMachineService providenciaStateMachineService;
    private final StateMachine stateMachine;
    private final UserRepository userRepository;
    private final PlazosHastaService plazosHastaService;
    private final DocumentoRepository documentoRepository;
    private final AdjuntoRepository adjuntoRepository;
    @Autowired
    private final AlfrescoStorageService alfrescoStorageService;


    public ProvidenciaService(
        ProvidenciaRepository providenciaRepository,
        ProvidenciaMapper providenciaMapper,
        AdjuntoMapper adjuntoMapper,
        ApplicationEventPublisher publisher,
        UserService userService,
        AdjuntoService adjuntoService,
        DerivacionRepository derivacionRepository,
        NotificacionInBrowserRepository notificacionInBrowserRepository,
        GrupoService grupoService,
        GrupoMapper grupoMapper,
        DocumentoMapper documentoMapper,
        DocumentoService documentoService,
        MovimientoProvidenciaService movimientoProvidenciaService,
        PlantillaService plantillaService,
        EntidadService entidadService,
        RespuestaService respuestaService, EntidadMapper entidadMapper,
        InvestigacionSumariaService investigacionSumariaService,
        SumarioAdministrativoService sumarioAdministrativoService,
        InvestigacionSumariaMapper investigacionSumariaMapper,
        SumarioAdministrativoMapper sumarioAdministrativoMapper,
        AlfrescoStorageService alfrescoStorageService,
        ProvidenciaStateMachineService providenciaStateMachineService,
        StateMachine stateMachine,
        UserRepository userRepository,
        PlazosHastaService plazosHastaService,
        DocumentoRepository documentoRepository, AdjuntoRepository adjuntoRepository) {
        this.providenciaRepository = providenciaRepository;
        this.providenciaMapper = providenciaMapper;
        this.adjuntoMapper = adjuntoMapper;
        this.publisher = publisher;
        this.userService = userService;
        this.adjuntoService = adjuntoService;
        this.derivacionRepository = derivacionRepository;
        this.notificacionInBrowserRepository = notificacionInBrowserRepository;
        this.grupoService = grupoService;
        this.grupoMapper = grupoMapper;
        this.documentoMapper = documentoMapper;
        this.documentoService = documentoService;
        this.movimientoProvidenciaService = movimientoProvidenciaService;
        this.plantillaService = plantillaService;
        this.entidadService = entidadService;
        this.respuestaService = respuestaService;
        this.entidadMapper = entidadMapper;
        this.investigacionSumariaService = investigacionSumariaService;
        this.sumarioAdministrativoService = sumarioAdministrativoService;
        this.investigacionSumariaMapper = investigacionSumariaMapper;
        this.sumarioAdministrativoMapper = sumarioAdministrativoMapper;
        this.alfrescoStorageService = alfrescoStorageService;
        this.providenciaStateMachineService = providenciaStateMachineService;
        this.stateMachine = stateMachine;
        this.userRepository = userRepository;
        this.plazosHastaService = plazosHastaService;

        this.documentoRepository = documentoRepository;
        this.adjuntoRepository = adjuntoRepository;
    }

    /**
     * Save a providencia.
     *
     * @param providenciaDTO the entity to save
     * @return the persisted entity
     */
    public ProvidenciaDTO save(ProvidenciaDTO providenciaDTO) {
        log.debug("Respuesta Save Providencia : {}", providenciaDTO);
        Providencia providencia = providenciaMapper.toEntity(providenciaDTO);
        EstadoProvidencia requisitoEstado = this.newState(providencia, AccionesProvidencia.CREAR_PROVIDENCIA);
        EstadoProvidencia subEtapa = this.determinaSubEtapa(requisitoEstado, EstadoProvidencia.NUEVA_PROVIDENCIA);
        EstadoProvidencia etapa = this.determinaEtapa(requisitoEstado);
        String estadoProviCompleto = this.concatenarEstado(requisitoEstado, subEtapa, etapa);
        String estadoInicial = this.determinaEstadoInicial(EstadoProvidencia.NUEVA_PROVIDENCIA);
        providencia.setFechaCreacion(Instant.now());
        providencia.setRequisito(requisitoEstado);
        providencia.setSubEtapa(subEtapa);
        providencia.setEtapa(etapa);
        providencia.setStandby(false);
        log.debug(" Estado completo es  " + estadoProviCompleto);
        log.debug(" EstadoInicial completo es   " + estadoInicial);
        providencia.setEstadoActual(estadoProviCompleto);


        // si la providencia tiene un tipo (SUMARIO y/o ADMINISTRATIVO) se debe instanciar ese tipo
        insertaTipoProvidencia(providencia);

        providencia = providenciaRepository.save(providencia);

        // Verifica que existan Adjuntos y los guarda
        verificaAdjuntosYGuarda(providenciaDTO, providencia);

        //Settear el ID de la providencia creada en los adjuntos debido a que providencia manda en la relación.
        List<AdjuntoDTO> adjuntoDTOs = this.setIdProvidenciaOnAdjuntos(providencia, providenciaDTO.getAdjuntos());

        providenciaDTO = this.providenciaMapper.toDto(providencia);

        if (adjuntoDTOs != null && adjuntoDTOs.size() > 0) {
            providenciaDTO.setAdjuntos(adjuntoDTOs.stream().collect(Collectors.toSet()));
        }

        Grupo groupAnswer = this.determineGroupAnswer(providencia);

        Derivacion derivacion = this.registerDerivation("desde el director nacional", providencia, null, groupAnswer);


        // si la creacion de la providecia se debe notificar se debe descomentar
//        this.registryNotificacion("desde el director nacional", groupAnswer);
        log.debug("variable xy", providencia.getNumeroDgd());
        this.movimientoProvidenciaService.save(estadoInicial, estadoProviCompleto, providencia.getId(), derivacion.getObservacion(),
            null, providenciaDTO.getAdjuntos(), "Derivado",
            (providenciaRepository.findnumeroDgd(providencia.getId())),
            (providenciaRepository.findnumeroDgdp(providencia.getId())));

        this.calcularPlazos(providencia);
        log.debug("variable nn", providenciaDTO);
        return providenciaDTO;
    }

    public void calcularPlazos(Providencia providencia){
        EstadoProvidencia requisito = providencia.getRequisito();
        log.debug("estos son los plazos");
        switch (requisito){
            case FORMULA_CARGOS:
                plazosHastaService.dias(providencia.getId(),2);
                break;

            case FORMULA_CARGOS_Y_NOTIFICA:
                plazosHastaService.dias(providencia.getId(),2);
                break;
            case PRORROGA1_CREADA:
                plazosHastaService.dias(providencia.getId(),20);
                break;
            case PRORROGA2_CREADA:
                plazosHastaService.dias(providencia.getId(),20);
                break;
            case INVESTIGACION:
                plazosHastaService.dias(providencia.getId(),20);
                break;
        }

    }

    //  Metodo que verifica que existan Adjuntos y los guarda
    private void verificaAdjuntosYGuarda(ProvidenciaDTO providenciaDTO, Providencia providencia) {
        if (providenciaDTO.getAdjuntos().size() > 0) {
            providencia.setAdjuntos(providenciaDTO.getAdjuntos().stream().map(this.adjuntoMapper::toEntity)
                .collect(Collectors.toSet()));
            this.alfrescoStorageService.moveToArchivosFolder(providenciaDTO.getAdjuntos().
                stream().map(AdjuntoDTO::getHash).collect(Collectors.toList()), providencia);
        }
    }

    // Metodo que verifica el tipo (SUMARIO y/o ADMINISTRATIVO) y lo instancia en la Providencia
    private void insertaTipoProvidencia(Providencia providencia) {
        if (providencia.getTipo() != null) {

            if (providencia.getTipo().equals(TipoProvidencia.INVESTIGACION_SUMARIA)) {
                InvestigacionSumaria investigacionSumaria = new InvestigacionSumaria();
                InvestigacionSumariaDTO investigacionSumariaDTO = this.investigacionSumariaService
                    .save(this.investigacionSumariaMapper.toDto(investigacionSumaria));

                investigacionSumaria = this.investigacionSumariaMapper.toEntity(investigacionSumariaDTO);
                providencia.setInvestigacionSumaria(investigacionSumaria);

            } else if (providencia.getTipo().equals(TipoProvidencia.SUMARIO_ADMINISTRATIVO)) {

                SumarioAdministrativo sumarioAdministrativo = new SumarioAdministrativo();
                SumarioAdministrativoDTO sumarioAdministrativoDTO = this.sumarioAdministrativoService
                    .save(this.sumarioAdministrativoMapper.toDto(sumarioAdministrativo));

                sumarioAdministrativo = this.sumarioAdministrativoMapper.toEntity(sumarioAdministrativoDTO);
                providencia.setSumarioAdministrativo(sumarioAdministrativo);
            }
        }
    }

    private String concatenarEstado(EstadoProvidencia requisitoEstado, EstadoProvidencia subEtapa, EstadoProvidencia etapa) {

        String estadoCompleto = null;
        estadoCompleto = etapa + " - " + subEtapa + " - " + requisitoEstado;
        return estadoCompleto;
    }

    private String determinaEstadoInicial(EstadoProvidencia requisito) {

        log.debug("Entrando a DeterminarEstadoInical" +  requisito);
        String estadoInicial = null;
        EstadoProvidencia requisitoInicial;
        EstadoProvidencia etapaInicial;
        EstadoProvidencia subEtapaInicial;

        if (requisito == EstadoProvidencia.PROVIDENCIA_SELECCION_FISCAL) {
            etapaInicial = requisito;
            requisitoInicial = EstadoProvidencia.NUEVA_PROVIDENCIA_;
            subEtapaInicial = this.determinaSubEtapa(requisitoInicial, requisito);
//
//        } else if (requisito == EstadoProvidencia.PROVIDENCIA_REABRIR || requisito == EstadoProvidencia.PROVIDENCIA_SANCION
//            || requisito == EstadoProvidencia.PROVIDENCIA_SOBRECEDER || requisito == EstadoProvidencia.PROVIDENCIA_ABSOLVER) {
//            etapaInicial = requisito;
//            requisitoInicial = EstadoProvidencia.NUEVA_PROVIDENCIA;
//            subEtapaInicial = this.determinaSubEtapa(requisitoInicial, requisito);
//
//        } else if (requisito == EstadoProvidencia.PROVIDENCIA_SANCION_APELO || requisito == EstadoProvidencia.PROVIDENCIA_SANCION_NO_APELO) {
//            etapaInicial = requisito;
//            requisitoInicial = EstadoProvidencia.NUEVA_PROVIDENCIA;
//            subEtapaInicial = this.determinaSubEtapa(requisitoInicial, requisito);

            // cuando se defina de que eta nace prorroga cambiar nuevaprovidencia
        } else if (requisito == EstadoProvidencia.PROVIDENCIA_PRORROGA_1 || requisito == EstadoProvidencia.PROVIDENCIA_PRORROGA_2) {
            etapaInicial = requisito;
            requisitoInicial = EstadoProvidencia.NUEVA_PROVIDENCIA_;
            subEtapaInicial = this.determinaSubEtapa(requisitoInicial, requisito);

        } else {
            requisitoInicial = requisito;
            subEtapaInicial = this.determinaSubEtapa(requisito, requisito);
            etapaInicial = this.determinaEtapa(requisito);
        }
        estadoInicial = this.concatenarEstado(requisitoInicial, subEtapaInicial, etapaInicial);

        log.debug("Saliendo de DeterminarEstadoInical" +  estadoInicial);
        return estadoInicial;
    }

    /**
     * Determina la SubEtapa en la que esta la providencia partiendo de los requesitosEstados.
     *
     * @param requisitoEstado requisito o Estado en el que se encuentra la providenia
     * @return subEtapa
     */
    private EstadoProvidencia determinaSubEtapa(EstadoProvidencia requisitoEstado, EstadoProvidencia etapa) {

        log.debug(" determinar subEtapa  con requisito " + requisitoEstado);
        EstadoProvidencia subEtapa = null;
        EstadoProvidencia requisito = requisitoEstado;

        switch (requisito) {


            case NUEVA_PROVIDENCIA:
            case NUEVA_PROVIDENCIA_:
                subEtapa = EstadoProvidencia.PROVIDENCIA_CREADA_;
                break;
            case NUEVA_PROVIDENCIA___:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL___;
                break;
            case FIRMA_PROVIDENCIA___:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL_____;
                break;
            case FIRMA_PROVIDENCIA____:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL____;
                break;
            case PROVIDENCIA_CREADA:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                break;
//            case REVISION_ASESOR:
//                subEtapa = EstadoProvidencia.ASESOR_DIRECTOR_NACIONAL;
//                break;
            case REVISION_PROVIDENCIA:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                break;
            case FIRMA_PROVIDENCIA:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                break;
            case ASIGNACION_NRO_INGRESO:
            case REVISA_PROVIDENCIA:
            case ASIGNACION_A_UPD:
            case DERIVA_ASIGNACION:
            case DERIVA_ASIGNACION___:
            case RESOLUCION_QUE_INSTRUYE_SUMARIO_MEMO_CONDUCTOR:
            case RESOLUCION_QUE_CONCEDE_PRORROGA_1:
            case REVISION_DE_RESOLUCION_Y_MEMO:
            case FIRMA_MEMO_VISA_RESOLUCION:
            case ENVIO_FIRMA_RESOLUCION_MEMO:
            case ASIGNACION_NRO_DESPACHO:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ___;
                break;
//            case REVISA_PROVIDENCIA:
//                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
//                break;
//            case ASIGNACION_A_UPD:
//                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
//                break;
//            case DERIVA_ASIGNACION:
//                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
//                break;
//            case RESOLUCION_QUE_INSTRUYE_SUMARIO_MEMO_CONDUCTOR:
////            case RESOLUCION_QUE_CONCEDE_PRORROGA_1:
//                subEtapa = EstadoProvidencia.RESOLUCION_Y_MEMO;
//                break;
//            case REVISION_DE_RESOLUCION_Y_MEMO:
//                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
//                break;
//            case FIRMA_MEMO_VISA_RESOLUCION:
//                subEtapa = EstadoProvidencia.FIRMA_SUBDIRECCION_JURIDICA;
//                break;
//            case ENVIO_FIRMA_RESOLUCION_MEMO:
//                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
//                break;
//            case ASIGNACION_NRO_DESPACHO:
//                subEtapa = EstadoProvidencia.FIRMA_SUBDIRECCION_JURIDICA;
//                break;
            case REVISA_RESOLUCION_Y_MEMO_:
                subEtapa = EstadoProvidencia.FIRMA_RESOLUCION_DIRECCION_NACIONAL;
                break;
            case REVISA_RESOLUCION_Y_MEMO:
                subEtapa = EstadoProvidencia.FIRMA_RESOLUCION_DIRECCION_NACIONAL;
                break;
            case FIRMA_DE_RESOLUCION_QUE_INSTRUYE_SUMARIO:
            case ENVIO_A_DGDP:
                subEtapa = EstadoProvidencia.FIRMA_DIRECTOR_NACIONAL;
                break;
            case ASIGNACION_No_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_1:
                subEtapa = EstadoProvidencia.No_RESOLUCION_DGDP;
                break;
            case COPIA_PRORROGA:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL_SJ;
                break;
            case ASIGNACION_DE_NUMERO_INGRESO__:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL_SJ;
                break;
            case ASIGNACION_NRO_DE_RESOLUCION_QUE_INSTRUYE_SUMARIO:
                subEtapa = EstadoProvidencia.NUMERO_DGDP;
                break;
//            case ASIGNACION_NRO_INGRESO_:
//                subEtapa = EstadoProvidencia.ELABORACION_NOTIFICACION_FISCAL;
//                break;
            case ASIGNACION_NRO_INGRESO_:
            case REDACCION_NOTIFICACION_FISCAL:
            case REVISION_NOTIFICACION_FISCAL:
            case FIRMA_NOTIFICACION_FISCAL:
            case ENVIO_A_DESPACHO:
            case ASIGNACION_NRO_DESPACHO_:
            case NOTIFICACION_FISCAL_:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL_SJ__;
                break;
//            case REVISION_NOTIFICACION_FISCAL:
//                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
//                break;
//            case FIRMA_NOTIFICACION_FISCAL:
//                subEtapa = EstadoProvidencia.ELABORACION_NOTIFICACION_FISCAL;
//                break;
//            case ENVIO_A_DESPACHO:
//                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
//                break;
//            case ASIGNACION_NRO_DESPACHO_:
//                subEtapa = EstadoProvidencia.ELABORACION_NOTIFICACION_FISCAL;
//                break;
//            case NOTIFICACION_FISCAL_:
//                subEtapa = EstadoProvidencia.ELABORACION_NOTIFICACION_FISCAL;
//                break;
            case FISCAL_NOTIFICADO:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL___;
                break;
            case FISCAL_NOTIFICADO__:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL___;
                break;
//            case GESTOR_DOCUMENTAL_ASIGNA_NUMERO:
//                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
//                break;
            case ASIGNACION_DE_NUMERO_INGRESO_:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ;
                break;
            case SUB_DIRECCION_DEBE_ASIGNAR:

                switch (etapa){
                    case NUEVA_PROVIDENCIA:
                        subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                        break;
                    case PROVIDENCIA_SELECCION_FISCAL:
                        subEtapa = EstadoProvidencia.CREADA_PROVIDENCIA_SELECCION_FISCAL;
                        break;
                    case PROVIDENCIA_PRORROGA_1:
                        subEtapa = EstadoProvidencia.CREADA_PROVIDENCIA_PRORROGA_1;
                        break;
                    case PROVIDENCIA_PRORROGA_2:
                        subEtapa = EstadoProvidencia.CREADA_PROVIDENCIA_PRORROGA_2;
                        break;
                }
                break;
            case ASIGNACION_A_UPD_:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ;
                break;
//                switch (etapa){
//                    case NUEVA_PROVIDENCIA:
//                        subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
//                        break;
//                    case PROVIDENCIA_SELECCION_FISCAL:
//                        subEtapa = EstadoProvidencia.CREADA_PROVIDENCIA_SELECCION_FISCAL;
//                        break;
//                    case PROVIDENCIA_PRORROGA_1:
//                        subEtapa = EstadoProvidencia.CREADA_PROVIDENCIA_PRORROGA_1;
//                        break;
//                    case PROVIDENCIA_PRORROGA_2:
//                        subEtapa = EstadoProvidencia.CREADA_PROVIDENCIA_PRORROGA_2;
//                        break;
//                }
//                break;

            case SECRETARIA_REVISA_NUMERO:
            case REVISA_PROVIDENCIA__:
            case SECRETARIA_REVISA_ASIGNACION:
            case DERIVA_ASIGNACION_:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ;
                break;
            case SECRETARIA_REVISA_FIRMA:
            case ENVIO_A_DGDP_:
                subEtapa = EstadoProvidencia.FIRMA_RESOLUCION_DIRECCION_NACIONAL;
                break;
            case ENVIO_FIRMA_RESOLUCION_MEMO_:
            case SECRETARIA_REVISA_RESOLUCION_Y_MEMO:
            case REVISION_DE_RESOLUCION_Y_MEMO_:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ;
                break;
            case SECRETARIA_REVISA_NOTIFICACION:
            case REVISO_NOTIFICACION_FISCAL:
            case FIRMA_COPIA_PRORROGA_1:
            case ENVIO_A_DESPACHO_:
            case DGD_ASIGNACION_No_DESPACHO:
            case NOTIFICA_COPIA_PRORROGA_1:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL_SJ;
                break;
            case INVESTIGACION_:
                subEtapa = EstadoProvidencia.DA_INICIO_FISCALIA_;
                break;
            case SECRETARIA_REVISA_FIRMA_NOTIFICACION:
            case SECRETARIA_REVISA_RESOLUCION_EXCENTA:
            case SECRETARIA_REVISA_FIRMA_RESOLUCION_EXCENTA:
                subEtapa = EstadoProvidencia.REVISION_SECRETARIA;
                break;
            case UPD_REDACTA_RESOLUCION_Y_MEMO:
            case RESOLUCION_CONCEDE_PRORROGA_1:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ;
                break;
            case ENVIAR_A_SUB_DIRRECION_JURIDICA:
            case UPD_REDACTA_RESOLUCION_EXCENTA_Y_MEMO:
                subEtapa = EstadoProvidencia.RESOLUCION_Y_MEMO;
                break;
            case ESPERANDO_FIRMA_VISA_DE_SUBDIRECCION:
            case FIRMA_MEMO_VISA_RESOLUCION_:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ;
                break;
            case DGD_DESPACHA_A_DN:
                subEtapa = EstadoProvidencia.FIRMA_SUBDIRECCION_JURIDICA;
                break;
            case ASIGNACION_No_DESPACHO:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ;
                break;
            case ESPERANDO_FIRMA_DEL_DN:
                subEtapa = EstadoProvidencia.FIRMA_DIRECTOR_NACIONAL;
                break;
            case FIRMA_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_1:
                subEtapa = EstadoProvidencia.FIRMA_RESOLUCION_DIRECCION_NACIONAL;
                break;
            case DGDP_ASIGNANDO_NUMERO:
            case CONTRALORIA_NOTIFICADA:
                subEtapa = EstadoProvidencia.NUMERO_DGDP;
                break;
            case DGD_RECEPCIONA:
            case UPD_ELABORA_NOTIFICACION_VISTA_FISCAL:
            case DGD_DESPACHA_NOTIFICACION_FISCAL:
            case ENVIADO_A_SUBDIRECCION_JURIDICA:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION:
            case UPD_NOTIFICA_FISCAL:
            case ELABORA_RESOLUCION_AFINATORIA_Y_MEMO:
            case REVISA_SECRETARIA_____________:
            case VISA_Y_FIRMA____:
            case REVISA_SECRETARIA______________:
            case NUMERO_DESPACHO:
            case REVISA_RESOLUCION_AFINATORIA_APLICA_SANCION_FINAL:
            case FIRMA_DIRECTOR_NACIONAL___:
            case DESPACHA_ASESOR__:
            case ASIGNACION_DE_NUMERO____:
            case RECEPCIONA_CONTRALORIA:
            case TOMA_DE_RAZON___:
            case REGISTRA__:
            case REPRESENTA__:
                subEtapa = EstadoProvidencia.SANCIONAR_NO_APELA;
                break;
//            case FISCAL_NOTIFICADO:
//                subEtapa = EstadoProvidencia.ELABORACION_NOTIFICACION_FISCAL;
//                break;
            case UPD_ELABORA_NOTIFICACION_PRORROGA_1:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION_PRORROGA_1:
            case UPD_NOTIFICA_PRORROGA_1_FISCAL:
            case DGD_DESPACHA_NOTIFICACION_PRORROGA_1_FISCAL:
                subEtapa = EstadoProvidencia.ELABORACION_NOTIFICACION_PRORROGA_1;
                break;

            case UPD_ELABORA_NOTIFICACION_PRORROGA_2:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION_PRORROGA_2:
            case UPD_NOTIFICA_PRORROGA_2_FISCAL:
            case DGD_DESPACHA_NOTIFICACION_PRORROGA_2_FISCAL:
                subEtapa = EstadoProvidencia.ELABORACION_NOTIFICACION_PRORROGA_2;
                break;

            case FISCAL_RECHAZO:
            case FISCAL_REDACTA_MEMO:
            case INVESTIGACION: //  le da una sub etapa al requisito
                subEtapa = EstadoProvidencia.DA_INICIO_FISCALIA;
                break;
            case INVESTIGACION_CERRADA: // se formulan cargos
            case INVESTIGACION_CERRADA_:
                subEtapa = EstadoProvidencia.DA_INICIO_FISCALIA;
                break;
            case REMITE_EXPEDIENTE_VISTA_FISCAL:
                subEtapa = EstadoProvidencia.SIN_DENUNCIANTE_FISCALIA;
                break;
            case INCULPADO_NOTIFICADO:
                subEtapa = EstadoProvidencia.FORMULA_CARGOS_FISCALIA;
                break;
            case FORMULA_CARGOS_Y_NOTIFICA:
                subEtapa = EstadoProvidencia.FORMULA_CARGOS_FISCALIA;
                break;
            case FORMULA_CARGOS:
//            case ENVIA_VISTA_FISCAL_A_DN:
            case REALIZA_DESCARGO:
                subEtapa = EstadoProvidencia.FORMULA_CARGOS;
                break;
//            case NUEVO_ESTADO:
            case ENVIA_VISTA_FISCAL_A_DN_:
                subEtapa = EstadoProvidencia.TERMINO_PROBATORIO;
                break;
            case ENVIA_VISTA_FISCAL_A_DN__:
                subEtapa = EstadoProvidencia.FORMULA_CARGOS_FISCALIA;
                break;
            case ENVIA_VISTA_FISCAL_A_DN___:
                subEtapa = EstadoProvidencia.FORMULA_CARGOS_FISCALIA;
                break;
//            case ASIGNACION_A_SJ:
//                subEtapa = EstadoProvidencia.VISTA_FISCAL_DIRECCION_NACIONAL;
//                break;
            case REVISION_ASIGNACION_Y_ENVIA_SJ:
                subEtapa = EstadoProvidencia.SUMARIO_COMPLETO;
                break;
            case ASIGNACION_DE_NUMERO_INGRESO:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case REVISA_SUMARIO_COMPLETO:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case ASIGNACION_DE_ABOGADO:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case REVISA_ASIGNACION:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case ELABORACION_DE_INFORME_JURIDICO:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case NO_REABRE:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case SI_DE_ACUERDO:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case NO_PROPONE:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case REVISA_INFORME:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case FIRMA_INFORME:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case ENVIA_INFORME_A_DESPACHO:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case No_DESPACHO_SJ:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case DESPACHO_COMPLETO:
                subEtapa = EstadoProvidencia.INFORME_SJ;
                break;
            case SOLICITUD_PRORROGA_2:
                subEtapa = EstadoProvidencia.DA_INICIO_FISCALIA;
                break;
            case REVISA_PRONUNCIAMIENTO:
            case ASIGNA_No_DGD:
            case REVISA_SECRETARIA_______:
            case REVISA_SECRETARIA________:
            case REVISA_SECRETARIA_________:
            case REVISA_SECRETARIA__________:
            case REVISA_SECRETARIA___________:
            case REVISA_SECRETARIA____________:
            case ASIGNACION_SUB_DIRECTORA:
            case RESOLUCION_QUE_PRONUNCIA_RECURSO_CON_MEMO_CONDUCTOR:
            case VISA_Y_FIRMA_:
            case DESPACHO_:
            case REVISA_RESOLUCION_QUE_PRONUNCIA:
            case FIRMA_DIRECTOR_NACIONAL_:
            case DESPACHA_ASESOR:
            case ASIGNACION_DE_NUMERO__:
            case ASIGNA_NUMERO_SJ_:
            case NOTIFICACION_RESULTADO_DE_RECURSO:
            case ELABORA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
            case VISA_Y_FIRMA__:
            case DESPACHO_DE_SJ:
            case REVISA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
            case FIRMA_DIRECTOR_NACIONAL__:
            case DESPACHA_ASESOR_:
            case ASIGNACION_DE_NUMERO___:
            case RECEPCION_CONTRALORIA:
            case RECEPCION_CONTRALORIA_TOMA_DE_RAZON_:
            case RECEPCION_CONTRALORIA_REGISTRA_:
            case RECEPCION_CONTRALORIA_REPRESENTA_:
                subEtapa = EstadoProvidencia.PRONUNCIANDO_RECURSO_;
                break;
            case RECIBE_DGDP:
            case RECEPCIONA_SJ:
            case ELABORA_NOTIFICACION:
            case SECRETARIA_REVISA___:
            case SECRETARIA_REVISA____:
            case VISA_Y_FIRMA___:
            case DESPACHO_SJ:
            case NOTIFICACION_DENUNCIADO:
            case ELABORA_MEMO_Y_REMUNERACION:
            case ELABORA_MEMO_DGDP:
            case ASIGNA_FOLIO:
                subEtapa = EstadoProvidencia.TOMA_DE_RAZON__;
                break;
            case RECIBE_DGDP__:
            case RECEPCIONA_SJ_:
            case ELABORA_NOTIFICACION_QUE_APLICA_SANCION_FINAL:
            case REVISA_SECRETARIA_______________:
            case VISA_Y_FIRMA_____:
            case REVISA_SECRETARIA_________________:
            case DESPACHO_SJ_:
            case NOTIFICA_DENUNCIADO:
            case ELABORA_MEMO_Y_REMUNERACION_:
            case ELABORA_MEMO_DGDP_:
            case ASIGNA_FOLIO_:
                subEtapa = EstadoProvidencia.TOMA_DE_RAZON_NO_APELA;
                break;
            case RECIBE_DGDP___:
            case RECEPCIONA_SJ__:
            case ELABORA_NOTIFICACION_:
            case SECRETARIA_REVISA_____:
            case VISA_Y_FIRMA______:
            case SECRETARIA_REVISA______:
            case DESPACHO_SJ__:
            case NOTIFICA_DENUNCIADO_:
            case ELABORA_MEMO_Y_REMUNERACION__:
            case ELABORA_MEMO_DGDP__:
            case ASIGNA_FOLIO__:
                subEtapa = EstadoProvidencia.REGISTRA_NO_APELA;
                break;
            case RECIBE_RECURSO:
            case REVISA_SECRETARIA______:
            case ASIGNA_ABOGADO:
            case SECRETARIA_REVISA:
            case INFORME_PRONUNCIADO:
            case INFORME_PRONUNCIADO_ACOGE_PARCIAL:
//            case ACOGE:

                // ----------- subetapas FISCAL ACOGE / NO ACOGE ---------

//            case ACOGE_INHABILIDAD:
//                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL___;
//                break;
//
//            case RECHAZA_INHABILIDAD:
//                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL____;
//                break;

                // ------------------------------------------------------
            case INFORME_PRONUNCIADO_ACOGE:
            case INFORME_PRONUNCIADO_RECHAZA:
            case SECRETARIA_REVISA_:
            case FIRMA_INFORME_:
            case SECRETARIA_REVISA__:
            case DESPACHO:
                subEtapa = EstadoProvidencia.SANCIONAR_SI_APELA;
                break;
            case TERMINO_PROBATORIO:
            case INCULPADO_ENVIA_MEMO:
            case INCULPADO_NO_ENVIA_MEMO:
            case FISCAL_REMITE_EXPEDIENTE:
            case REMITE_VISTA_FISCAL:
                if (etapa==EstadoProvidencia.INVESTIGACION || etapa==EstadoProvidencia.INVESTIGACION_PRORROGA_1
                    || etapa==EstadoProvidencia.INVESTIGACION_PRORROGA_2){
                    subEtapa = EstadoProvidencia.DA_INICIO_FISCALIA;
                }
                break;
//            case ASIGNACION_A_SJ:
            case ENVIAR_SUMARIO_A_SUB_DIRECCION:
            case DGD_DESPACHA_SUMARIO:

            case SECRETARIA_REVISA_SUMARIO:
            case SUB_DIRECCION_ASIGNA_ABOGADO:
            case SECRETARIA_REVISA_SUMARIO_Y_NOTIFICA_A_ABOGADO:
            case ABOGADO_ELABORA_INFORME:
                subEtapa = EstadoProvidencia.ASIGNACION_SJ;
                break;

//            case SI_DE_ACUERDO:
//            case NO_REABRE:
//            case NO_PROPONE:
            case SECRETARIA_REVISA_INFORME:
            case SUB_DIRECCION_REVISA_INFORME:
            case SECRETARIA_DESPACHA_INFORME:
            case DGD_DESPACHA_SUMARIO_COMPLETO:
                subEtapa = EstadoProvidencia.ABOGADO_RESPONDE;
                break;

            case FORMULA_CARGOS_TERMINO_PROBATORIO:
            case APELACION_INCULPADO:
            case UPD_ELABORA_NOTIFICACION:
            case ESPERANDO_FIRMA_VISA_DE_SUBDIRECCION_A_NOTIFICACION:
            case UPD_NOTIFICA_A_INCULPADO:
                subEtapa = EstadoProvidencia.FISCAL;
                break;
            case DN_RECIBE_SUMARIO_COMPLETO:
            case REVISION_SUMARIO_COMPLETO:
            case DGD_RECEPCIONA_SUMARIO:
                subEtapa = EstadoProvidencia.VISTA_FISCAL;
                break;
            case UPD_REALIZA_MEMO:
                subEtapa = EstadoProvidencia.MEMO;
                break;
            case PETICION_APELACION:
                subEtapa = EstadoProvidencia.APELACION_SOLICITADA;
                break;

            case PETICION_PRORROGA_1:
            case PETICION_PRORROGA_2:
                subEtapa = EstadoProvidencia.PRORROGA_SOLICITADA;
                break;

            case PROVIDENCIA_CREADA_:
            case FIRMA_PROVIDENCIA_:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DN;
                break;
            case REVISA_PROVIDENCIA_:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DN;
                break;
            case REVISION_DE_PROVIDENCIA_:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DN_;
                break;
            // --- prorroga 2 ------

            case REVISION_DE_PROVIDENCIA___:
            case FIRMA_PROVIDENCIA__:
                subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL;
                break;
            case ASIGNACION_DE_NUMERO_INGRESO___:
            case REVISION_DE_PROVIDENCIA____:
            case ASIGNACION_A_UPD__:
            case DERIVA_ASIGNACION__:
            case RESOLUCION_CONCEDE_PRORROGA_2:
            case REVISION_DE_RESOLUCION_Y_MEMO__:
            case FIRMA_MEMO_VISA_RESOLUCION__:
            case ENVIO_FIRMA_RESOLUCION_MEMO__:
            case ASIGNACION_No_DESPACHO__:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ__;
                break;
            case REVISA_RESOLUCION_Y_MEMO__:
            case FIRMA_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_2:
            case ENVIO_A_DGDP__:
                subEtapa = EstadoProvidencia.FIRMA_RESOLUCION_DIRECCION_NACIONAL_;
                break;
            case ASIGNACION_No_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_2:
                subEtapa = EstadoProvidencia.No_RESOLUCION_DGDP_;
                break;
            case ASIGNACION_DE_NUMERO_INGRESO____:
            case COPIA_PRORROGA_2:
            case REVISION_NOTIFICACION_FISCAL_:
            case FIRMA_COPIA_PRORROGA_2:
            case ENVIO_A_DESPACHO__:
            case DGD_ASIGNA_No_DESPACHO___:
            case NOTIFICA_COPIA_PRORROGA_2:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL_SJ_;
                break;
            case INVESTIGACION__:
                subEtapa = EstadoProvidencia.DA_INICIO_FISCALIA__;
                break;
//            case SOLICITUD_PRORROGA_2:

            // ----- ACOGE RECURSO ---------
            case ASIGNACION_DE_NUMERO_INGRESO_____:
            case REVISA_PROVIDENCIA_______:
            case ASIGNACION_A_UPD___:
            case DERIVA_ASIGNACION____:
            case RESOLUCION_QUE_ACOGE_INHABILIDAD:
            case REVISION_DE_RESOLUCION_Y_MEMO___:
            case FIRMA_MEMO_VISA_RESOLUCION___:
            case ENVIO_FIRMA_RESOLUCION_MEMO___:
            case ASIGNACION_No_DESPACHO________:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ____;
                break;
            case REVISA_RESOLUCION_Y_MEMO___:
            case RESOLUCION_QUE_ACOGE_INHABILIDAD_:
            case ENVIO_A_DGDP___:
                subEtapa = EstadoProvidencia.FIRMA_RESOLUCION_DIRECCION_NACIONAL__;
                break;
            case ASIGNACION_No_RESOLUCION:
                subEtapa = EstadoProvidencia.NUMERO_RESOLUCION_DGDP;
                break;
            case ASIGNACION_No_DE_INGRESO_:
            case REDACION_NOTIFICACION_FISCAL:
            case REVISION_NOTIFICACION_FISCAL__:
            case FIRMA_NOTIFICACION_FISCAL_:
            case ENVIO_A_DESPACHO___:
            case ASIGNACION_No_DESPACHO_________:
            case NOTIFICACION_FISCAL____:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL_SJ____;
                break;

                // ---- NO ACOGE RECURSO ----
            case ASIGNACION_DE_NUMERO_INGRESO______:
            case REVISA_PROVIDENCIA________:
            case ASIGNACION_A_UPD____:
            case DERIVA_ASIGNACION_____:
            case RESOLUCION_QUE_RECHAZA_INHABILIDAD:
            case REVISION_DE_RESOLUCION_Y_MEMO____:
            case FIRMA_MEMO_VISA_RESOLUCION____:
            case ENVIO_FIRMA_RESOLUCION_MEMO____:
            case ASIGNACION_No_DESPACHO______:
                subEtapa = EstadoProvidencia.RESOLUCION_SJ______;
                break;
            case REVISA_RESOLUCION_Y_MEMO____:
            case RESOLUCION_QUE_RECHAZA_INHABILIDAD_:
            case ENVIO_A_DGDP____:
                subEtapa = EstadoProvidencia.FIRMA_RESOLUCION_DIRECCION_NACIONAL___;
                break;
            case ASIGNACION_No_RESOLUCION_:
                subEtapa = EstadoProvidencia.NUMERO_RESOLUCION_DGDP_;
                break;
            case ASIGNACION_No_DE_INGRESO__:
            case REDACION_NOTIFICACION_FISCAL_:
            case REVISION_NOTIFICACION_FISCAL___:
            case FIRMA_NOTIFICACION_FISCAL__:
            case ENVIO_A_DESPACHO____:
            case ASIGNACION_No_DESPACHO_______:
            case NOTIFICACION_FISCAL_____:
                subEtapa = EstadoProvidencia.NOTIFICACION_FISCAL_SJ___;
                break;
        }
        return subEtapa;
    }

    /**
     * Determina la Etapa partiendo de la subEtapa
     *
     * @param requisitoActual requisitoActual en la que se encuentra la providenciaNueva o la ProvidenciaMadre
     * @return etapa correspondiente al requisitoActual
     */
    private EstadoProvidencia determinaEtapa(EstadoProvidencia requisitoActual) {
        log.debug(" determinar Etapa  con REQUISITO " + requisitoActual);
        EstadoProvidencia etapa = null;
        EstadoProvidencia requisito = requisitoActual;

        switch (requisito) {
            // Setea variable Etapa segun la subEtapa en que se encuentre
            case NUEVA_PROVIDENCIA:
            case PROVIDENCIA_CREADA:
                etapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                break;
            case PROVIDENCIA_CREADA_:
                etapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                break;
            case FISCAL_RECHAZO:
                etapa = EstadoProvidencia.PROVIDENCIA_SELECCION_FISCAL;
                break;
            case DGD_DESPACHA_NOTIFICACION_FISCAL:
                etapa = EstadoProvidencia.INVESTIGACION;
                break;

                // -------- FISCAL ACOGE / NO ACOGE ----------
            case FIRMA_PROVIDENCIA___:
                etapa = EstadoProvidencia.ACOGE_INHABILIDAD;
                break;

            case FIRMA_PROVIDENCIA____:
                etapa = EstadoProvidencia.RECHAZA_INHABILIDAD;
                break;

            case FISCAL_NOTIFICADO:
                etapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                break;
        }
        log.debug(" La Etapa es " + etapa);
        return etapa;
    }

    /**
     * @CreatedBy Ruben Barrera
     * @param requisitoActual
     * @param etapaActual
     * @return
     *
     * determinar etapa en el flujo
     *
     */

    private EstadoProvidencia determinaEtapaenFlujo(EstadoProvidencia subEtapaActual,EstadoProvidencia requisitoActual, EstadoProvidencia etapaActual) {
        log.debug(" determinar Etapa en flujo  con REQUISITO " + requisitoActual);
        log.debug(" determinar Etapa en flujo  con ETAPAaCTUAL " + etapaActual);
        EstadoProvidencia etapa = etapaActual;
        EstadoProvidencia requisito = requisitoActual;
        EstadoProvidencia subEtapa = subEtapaActual;
        log.debug(" por aqui pase ");

        switch (requisito) {
            // Setea variable Etapa segun la subEtapa en que se encuentre
            case INVESTIGACION:
                switch (etapaActual){
                    case PROVIDENCIA_PRORROGA_1:
                        etapa = EstadoProvidencia.INVESTIGACION_PRORROGA_1;
                        break;
                    case PROVIDENCIA_PRORROGA_2:
                        etapa = EstadoProvidencia.INVESTIGACION_PRORROGA_2;
                        break;
                    case NUEVA_PROVIDENCIA:
                    case PROVIDENCIA_SELECCION_FISCAL:
                        etapa = EstadoProvidencia.INVESTIGACION;
                }
                break;
//
//            case PROVIDENCIA_CREADA____:
//                switch (etapaActual) {
//                    case PROVIDENCIA_SELECCION_FISCAL:
//                        if (subEtapaActual == EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL_____){
//                            etapa = EstadoProvidencia.ACOGE_INHABILIDAD;
//                        } else{
//                            etapa = EstadoProvidencia.RECHAZA_INHABILIDAD;
//                        }
//                        break;
//                }
//                break;

            case PROVIDENCIA_CREADA____:
                switch (etapaActual) {
                    case PROVIDENCIA_SELECCION_FISCAL:
                            etapa = EstadoProvidencia.ACOGE_INHABILIDAD;
                        break;
                }
                break;

            case FIRMA_PROVIDENCIA___:
                switch (etapaActual) {
                    case PROVIDENCIA_SELECCION_FISCAL:
                        etapa = EstadoProvidencia.ACOGE_INHABILIDAD;
                        break;
                }
                break;

            case FISCAL_NOTIFICADO:
                switch (etapaActual) {
                    case ACOGE_INHABILIDAD:
                        etapa = EstadoProvidencia.NUEVA_PROVIDENCIA;
                        break;
                }
                break;



            case FIRMA_PROVIDENCIA____:
                switch (etapaActual) {
                    case PROVIDENCIA_SELECCION_FISCAL:
                        etapa = EstadoProvidencia.RECHAZA_INHABILIDAD;
                        break;
                }
                break;

            case FORMULA_CARGOS_Y_NOTIFICA:
                switch (etapaActual){
                    case INVESTIGACION_PRORROGA_1:
                    case INVESTIGACION_PRORROGA_2:
                        etapa = etapaActual;
                        break;
                }
                break;

            case FORMULA_CARGOS:
                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){
                    etapa = EstadoProvidencia.INVESTIGACION;
                }
                break;

            case PROVIDENCIA_CREADA_:
                if (subEtapa== EstadoProvidencia.NUEVA_PROVIDENCIA_DN){
                    etapa = EstadoProvidencia.INVESTIGACION_PRORROGA_1;
                }
                break;

//            case FISCAL_RECHAZO :
//                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){
//                    etapa = EstadoProvidencia.INVESTIGACION_PRORROGA_1;
//                }
//                break;

            case REVISION_DE_PROVIDENCIA_:
                if (subEtapa== EstadoProvidencia.NUEVA_PROVIDENCIA_DN_){
                    etapa = EstadoProvidencia.INVESTIGACION_PRORROGA_2;
                }
                break;
        }
        log.debug(" La Etapa en el flujo  es " + etapa);
        return etapa;
    }

    @Transactional
    public ProvidenciaDTO update(ProvidenciaDTO providenciaDTO, Set<AdjuntoDTO> adjuntoDTOs) {
        Providencia providencia = this.providenciaMapper.toEntity(providenciaDTO);

        log.debug("Dentro del update , id de la madre es " + providencia.getProvidencia_madre_id());

        // si la providencia tiene un tipo, se debe; instanciar ese tipo y relacionarlo con la providencia
        if (providencia.getTipo() != null) {
            if (providencia.getTipo().equals(TipoProvidencia.INVESTIGACION_SUMARIA) && providencia.getInvestigacionSumaria() == null) {
                InvestigacionSumaria investigacionSumaria = new InvestigacionSumaria();
                InvestigacionSumariaDTO investigacionSumariaDTO = this.investigacionSumariaService
                    .save(this.investigacionSumariaMapper.toDto(investigacionSumaria));

                investigacionSumaria = this.investigacionSumariaMapper.toEntity(investigacionSumariaDTO);

                providencia.setInvestigacionSumaria(investigacionSumaria);
            } else if (providencia.getTipo().equals(TipoProvidencia.SUMARIO_ADMINISTRATIVO) && providencia.getSumarioAdministrativo() == null) {
                SumarioAdministrativo sumarioAdministrativo = new SumarioAdministrativo();
                SumarioAdministrativoDTO sumarioAdministrativoDTO = this.sumarioAdministrativoService
                    .save(this.sumarioAdministrativoMapper.toDto(sumarioAdministrativo));

                sumarioAdministrativo = this.sumarioAdministrativoMapper.toEntity(sumarioAdministrativoDTO);
                providencia.setSumarioAdministrativo(sumarioAdministrativo);
            }
        }

        this.providenciaRepository.save(providencia);

        if (adjuntoDTOs != null && adjuntoDTOs.size() > 0) {
            //Settear el ID de la providencia creada en los adjuntos debido a que providencia manda en la relación.
            List<AdjuntoDTO> adjuntos = this.setIdProvidenciaOnAdjuntos(providencia, adjuntoDTOs);

            providenciaDTO = this.providenciaMapper.toDto(providencia);
            providenciaDTO.setAdjuntos(adjuntos.stream().collect(Collectors.toSet()));
        }
        log.debug("saliendo del update " + providenciaDTO);
        return providenciaDTO;
    }

    @Transactional
    public ProvidenciaDTO updateNumeroDGD(ProvidenciaUpdateNumeroReferenciaDTO providenciaUpdateNumeroReferenciaDTO) {

        log.debug("Asignar numero DGD paso: ");
        Providencia providencia = null;

        if (providenciaUpdateNumeroReferenciaDTO.getProvidenciaId() != null && providenciaUpdateNumeroReferenciaDTO
            .getProvidenciaId() > 0 && providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() != null &&
            providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() > 0) {

            this.providenciaRepository.updateNumeroDGD(providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia(),
                providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
            providencia = this.providenciaRepository.getOne(providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
        }
        return this.providenciaMapper.toDto(providencia);
    }

    @Transactional
    public ProvidenciaDTO updateNumeroDGDP(ProvidenciaUpdateNumeroReferenciaDTO providenciaUpdateNumeroReferenciaDTO) {

        log.debug("Asignar numero DGDP paso: ");
        Providencia providencia = null;

        if (providenciaUpdateNumeroReferenciaDTO.getProvidenciaId() != null && providenciaUpdateNumeroReferenciaDTO
            .getProvidenciaId() > 0 && providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() != null &&
            providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() > 0) {

            this.providenciaRepository.updateNumeroDGDP(providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia(),
                providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
            providencia = this.providenciaRepository.getOne(providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
        }
        return this.providenciaMapper.toDto(providencia);
    }

    @Transactional
    public ProvidenciaDTO updateNumeroProvidencia (ProvidenciaUpdateNumeroReferenciaDTO providenciaUpdateNumeroReferenciaDTO) {

        log.debug("Asignar numero Providencia paso: ");
        Providencia providencia = null;

        if (providenciaUpdateNumeroReferenciaDTO.getProvidenciaId() != null && providenciaUpdateNumeroReferenciaDTO
            .getProvidenciaId() > 0 && providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() != null &&
            providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() > 0) {

            this.providenciaRepository.updateNumeroProvidencia(providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia(),
                providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
            providencia = this.providenciaRepository.getOne(providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
        }
        return this.providenciaMapper.toDto(providencia);
    }

    @Transactional
    public ProvidenciaDTO updateFolio(ProvidenciaUpdateNumeroReferenciaDTO providenciaUpdateNumeroReferenciaDTO) {

        log.debug("Asignar Folio paso: ");
        Providencia providencia = null;

        if (providenciaUpdateNumeroReferenciaDTO.getProvidenciaId() != null && providenciaUpdateNumeroReferenciaDTO
            .getProvidenciaId() > 0 && providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() != null &&
            providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() > 0) {

            this.providenciaRepository.updateNumeroFolio(providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia(),
                providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
            providencia = this.providenciaRepository.getOne(providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
        }
        return this.providenciaMapper.toDto(providencia);
    }

    @Transactional(readOnly = true)
    public Optional<DetalleProvidenciaDTO> findOneDetalle(Long id) {
        log.debug("resultado del findOneDetalle: {}", id);

        return providenciaRepository.findById(id).map(providencia -> {
            Integer sumaAdjuntos =adjuntoRepository.Contar(providencia.getId())+documentoRepository.Contar(providencia.getId());

            log.debug("ruben-cantidad adjuntos: {}", sumaAdjuntos);
//            this.providenciaMapper
//                .toDto(this.providenciaRepository.getOne (providencia.getId())
//           );

            return new DetalleProvidenciaDTO(
                providencia.getId(),
                providencia.getNumeroReferencia(),
                providencia.getNumeroProvidencia(),
                providencia.getNumeroDgdp(),
                providencia.getFolio(),
                providencia.getNumeroDgd(),
                providencia.getEstadoActual(),
                providencia.getEtapa(),
                providencia.getSubEtapa(),
                providencia.getRequisito(),
                providencia.getTipo(),
                providencia.getComentario(),
                providencia.getFechaSolicitud(),
                providencia.getFechaCreacion(),
                providencia.getInstrucciones(),
//                providencia.getSumarioAdministrativo().getId(),
//                providencia.getInvestigacionSumaria().getId(),
                providencia.getFechaHasta(),
                providencia.getRunSolicitante(),
                providencia.getNombreSolicitante(),
                providencia.getRunImplicado(),
                providencia.getNombreImplicado(),
                providencia.getNombreFiscalAsignado(),
                providencia.getProvidencia_madre_id(),
                providencia.getStandby(),
                sumaAdjuntos);
        });
    }


    /**
     * Get all the providencias.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ProvidenciaItemListDTO> findAll(Pageable pageable) {

        return providenciaRepository.findAll(pageable).map(providencia -> {

            String nombreGrupo = this.determineGroupAnswer(providencia).getNombre();
            MovimientoProvidencia  mp =  movimientoProvidenciaService.buscarPorId(providencia.getId());
            log.debug(" resumen resultado del movimiento: "+mp);

//            Long sinresultados= Long.valueOf(0);
            /**
             * si agregas mas cosas al providenciaitemlistdto debes seguir el orden  del dto cuando muestres los datos aca
             */
//            if (mp != null) {
                return new ProvidenciaItemListDTO(
                    providencia.getId(),
                    providencia.getFechaCreacion(),
                    providencia.getEstadoActual(),
                    nombreGrupo,
                    ChronoUnit.DAYS.between(providencia.getFechaCreacion(), Instant.now()),
                    ChronoUnit.DAYS.between(providencia.getFechaCreacion(),mp.getFecha()),
                    providencia.getFechaHasta()


                );
            });
    }
//

    /**
     * Get all the Providencia with eager load of many-to-many relationships.
     *
     * @return the list of entities
     */
    public Page<ProvidenciaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return providenciaRepository.findAll(pageable).map(this.providenciaMapper::toDto);
    }

    /**
     * Get one providencia by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<ProvidenciaDTO> findOne(Long id) {
        log.debug("resultado del fynById: {}", id);
        return providenciaRepository.findById(id).map(this.providenciaMapper::toDto);
    }

    //
    // buscar por numero de referencia y traer todas las providencias ccon ese numero
    @Transactional(readOnly = true)
    public Set<ProvidenciaDTO> findAllNro() {
        return providenciaRepository.findByAllnroReferencia().stream().map(this.providenciaMapper::toDto).collect(Collectors.toSet());
    }
    /**
     * Delete the providencia by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Providencia : {}", id);
        providenciaRepository.deleteById(id);
    }

    /*
     Método que permite settear el id de la providencia en los adjuntos que colaboran con el objeto.
     */
    private List<AdjuntoDTO> setIdProvidenciaOnAdjuntos(Providencia providencia,  Set<AdjuntoDTO> adjuntosDTOs) {
        if (adjuntosDTOs != null && adjuntosDTOs.size() > 0) {
            for (Iterator<AdjuntoDTO> it = adjuntosDTOs.iterator(); it.hasNext(); ) {
                AdjuntoDTO adjuntoDTO = it.next();
                if (adjuntoDTO.getProvidenciaId() == null) {
                    adjuntoDTO.setProvidenciaId(providencia.getId());
                    this.adjuntoService.save(adjuntoDTO);
                }
            }

            return this.adjuntoService.getByProvidencia(providencia).stream().map(this.adjuntoMapper::toDto)
                .collect(Collectors.toList());
        }

        return null;
    }

    /*
     Método que permite registrar una derivación.
     */

    private Derivacion registerDerivation(String observacion, Providencia providencia, User derivadoAUsuario, Grupo derivadoAGrupo) {

        Derivacion derivacion = new Derivacion();

        derivacion.setObservacion(observacion);
        derivacion.setProvidencia(providencia);
        derivacion.setDerivadoAUsuario(derivadoAUsuario);
        derivacion.setDerivadoPorUsuario(this.userService.getCurrentUser());
        derivacion.setDerivadoAGrupo(derivadoAGrupo);
        derivacion.setDerivadoPorGrupo(this.userService.getCurrentUser().getGrupo());
        derivacion.setEstado(EstadoDerivacion.NO_LEIDO);
        derivacion = this.derivacionRepository.save(derivacion);

        return derivacion;
    }

    // notificaciones created by: Ruben Barrera
    private NotificacionInBrowser registryNotificacion(String observacion, Grupo derivadoAGrupo, AccionesProvidencia evento) {


        List<Long> usuariosUNOporUNO=this.userRepository.findByAllGrupo(derivadoAGrupo.getId());


        for (int i= 0; i < usuariosUNOporUNO.size(); i++){

            NotificacionInBrowser notificacion =  new NotificacionInBrowser();

            log.debug("en el foreach este es el id de los usuarios del grupo: "+usuariosUNOporUNO.get(i));

            switch (evento){
                default:
                    notificacion.setContenido(observacion);
                    break;
                case FISCAL_NOTIFICA_A_UPD_CIERRE:
                    notificacion.setContenido("Fiscal A dado Cierre a Investigacion de la Providencia: ");
                    break;
                case FISCAL_ACEPTA:
                    notificacion.setContenido("Fiscal A Aceptado  Investigacion de la Providencia: ");
                    break;
                case NOTIFICA:
                    notificacion.setContenido("Se Notifico al Demandante: ");
                    break;
                case NOTIFICA_INCULPADO:
                    notificacion.setContenido("Se Notifico al Inculpado: ");
                    break;
            }

            notificacion.setGrupo(derivadoAGrupo);
            notificacion.setCreatedAt(Instant.now());
            notificacion.setVisto(false);
            Optional<User> usuario = this.userRepository.findById(usuariosUNOporUNO.get(i));
            notificacion.setUser(usuario.get());
            log.debug("ESTA ES LA NOTIFICACION: "+notificacion);
            notificacion = this.notificacionInBrowserRepository.save(notificacion);

        }
        return  null;
    }
    /**
     * BOTONES que ENVIA UN EVENTO PARA CONTINUAR FLUJO DE ESTADOS.
     *
     * @param providenciaResponseDTO
     */
    @Transactional
    public void reply(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton CONTINUAR paso: ");
        AccionesProvidencia evento = AccionesProvidencia.CREAR_PROVIDENCIA;
        this.changeStage(providenciaResponseDTO, evento);
    }

//    @Transactional
//    public void fiscalNotificaCierre(ProvidenciaResponseDTO providenciaResponseDTO) {
//        log.debug("boton desde fiscal acepta y da inicio paso: ");
//        AccionesProvidencia evento = AccionesProvidencia.FISCAL_NOTIFICA_A_UPD_CIERRE;
//        this.changeStage(providenciaResponseDTO, evento);
//    }

    @Transactional
    public void fiscalNotificaCierre(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton desde fiscal acepta y da inicio paso: ");
        AccionesProvidencia evento = null;
        if (providenciaResponseDTO.getRequisito() == EstadoProvidencia.INVESTIGACION__) {
            evento = AccionesProvidencia.INVESTIGACION_;
        }else{
            evento = AccionesProvidencia.FISCAL_NOTIFICA_A_UPD_CIERRE;
        }
        this.changeStage(providenciaResponseDTO, evento);
    }


    @Transactional
    public void formularCargos(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton formularCargos: ");
        AccionesProvidencia evento = AccionesProvidencia.FORMULAR_CARGOS;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void terminoProbatorio (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton terminoProbatorio: ");
        AccionesProvidencia evento = AccionesProvidencia.TERMINO_PROBATORIO;
//        AccionesProvidencia evento = AccionesProvidencia.ENVIA_VISTA_FISCAL_A_DN;
        this.changeStage(providenciaResponseDTO, evento);
    }


    @Transactional
    public void enviaVistaFiscal (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton enviaVistaFiscal: ");
        AccionesProvidencia evento = AccionesProvidencia.ENVIA_VISTA_FISCAL;
//        AccionesProvidencia evento = AccionesProvidencia.ENVIA_VISTA_FISCAL_A_DN;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void siDeAcuerdo (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton siDeAcuerdo: ");
        AccionesProvidencia evento = AccionesProvidencia.SI_DE_ACUERDO;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void acogeParcial (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton acogeParcial: ");
        AccionesProvidencia evento = AccionesProvidencia.INFORME_PRONUNCIADO_ACOGE_PARCIAL;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void acoge (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton acoge: ");
        AccionesProvidencia evento = AccionesProvidencia.INFORME_PRONUNCIADO_ACOGE;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void rechaza (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton rechaza: ");
        AccionesProvidencia evento = AccionesProvidencia.INFORME_PRONUNCIADO_RECHAZA;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void noReabro(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton noReabro: ");
        AccionesProvidencia evento = AccionesProvidencia.NO_REABRE;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void noPropone (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton noPropone: ");
        AccionesProvidencia evento = AccionesProvidencia.NO_PROPONE;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void remiteExpediente(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton de fiscal remite expdiente: ");
        AccionesProvidencia evento = AccionesProvidencia.REMITE_EXPEDIENTE;
        this.changeStage(providenciaResponseDTO, evento);
    }

//    @Transactional
//    public void remiteExpediente(ProvidenciaResponseDTO providenciaResponseDTO) {
//        log.debug("boton de fiscal remite expdiente: ");
//        AccionesProvidencia evento = AccionesProvidencia.REMITE_EXPEDIENTE;
//        this.changeStage(providenciaResponseDTO, evento);
//    }

    @Transactional
    public void sinDenunciante(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton de  sinDenunciante: ");
        AccionesProvidencia evento = AccionesProvidencia.SIN_DENUNCIANTE;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void aceptar(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton ACEPTAR paso: ");
        AccionesProvidencia evento = AccionesProvidencia.FISCAL_ACEPTA;
        this.changeStage(providenciaResponseDTO, evento);
    }

    // ------------- Fiscal Acoge / No Acoge ----------

    @Transactional
    public void acogeFiscal(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton ACOGEFISCAL paso: ");
        AccionesProvidencia evento = AccionesProvidencia.FISCAL_ACOGE;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void noAcogeFiscal(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton NOACOGEFISCAL paso: ");
        AccionesProvidencia evento = AccionesProvidencia.FISCAL_NO_ACOGE;
        this.changeStage(providenciaResponseDTO, evento);
    }

    // ----------------------------------------------------

    @Transactional
    public void rechazar(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton RECHAZAR paso: ");
        AccionesProvidencia evento = AccionesProvidencia.FISCAL_RECHAZA;
        this.changeStage(providenciaResponseDTO, evento);
    }

//    @Transactional
//    public void rechazar(ProvidenciaResponseDTO providenciaResponseDTO) {
//        log.debug("boton RECHAZAR paso: ");
//        AccionesProvidencia evento = null;
//        if (providenciaResponseDTO.getRequisito() == EstadoProvidencia.FISCAL_NOTIFICADO__) {
//            evento = AccionesProvidencia.FISCAL_ACOGE;
//        }else if(providenciaResponseDTO.getRequisito() == EstadoProvidencia.FISCAL_NOTIFICADO){
//            evento = AccionesProvidencia.FISCAL_RECHAZA;
//        }
//        this.changeStage(providenciaResponseDTO, evento);
//    }

    @Transactional
    public void prorroga(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton PRORROGA paso: ");
        AccionesProvidencia evento = null;
        if (providenciaResponseDTO.getRequisito() == EstadoProvidencia.INVESTIGACION_) {
//        AccionesProvidencia evento = AccionesProvidencia.FLUJO_REGISTRA;
            evento = AccionesProvidencia.INVESTIGACION_;
        } else {
            evento = AccionesProvidencia.PRORROGA;
        }
        this.changeStage(providenciaResponseDTO, evento);
    }

//    @Transactional
//    public void prorroga(ProvidenciaResponseDTO providenciaResponseDTO) {
//        log.debug("boton PRORROGA paso: ");
//        AccionesProvidencia evento = AccionesProvidencia.PRORROGA;
//        this.changeStage(providenciaResponseDTO, evento);
//    }

    // BOTON REPRESENTA PARA FLUJO DE SANCION NO APELA luego envia memo
    @Transactional
    public void inculpadoEnviaMemo(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton inculado envia memo paso: ");
        AccionesProvidencia evento = AccionesProvidencia.INCULPADO_ENVIA_MEMO;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void inculpadoNoEnviaMemo(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton NO APELA paso: ");
        AccionesProvidencia evento = AccionesProvidencia.INCULPADO_NO_ENVIA_MEMO;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void notificaDemandado(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton notificaDemandado o inculpadoNotificado paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();

        if (providencia.getRequisito()==EstadoProvidencia.NOTIFICAR_INCULPADO){
            log.debug("ENTRO notificarInculpado con la provi " + providencia);
            AccionesProvidencia evento = AccionesProvidencia.NOTIFICA_INCULPADO;
            Grupo groupAnswer = this.determineGroupAnswer(providencia);
            providencia.setRequisito(EstadoProvidencia.SE_NOTIFICO_INCULPADO);
            providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
            this.registryNotificacion("pendiente por hacer " + providencia.getRequisito(), groupAnswer, evento);
        }else {
            AccionesProvidencia evento = AccionesProvidencia.NOTIFICA;
            this.changeStage(providenciaResponseDTO, evento);
        }
    }

    // BOTON REPRESENTA PARA FLUJO DE SANCION APELA
    @Transactional
    public void apela(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton APELA paso: ");
        AccionesProvidencia evento = AccionesProvidencia.FLUJO_APELA;
        this.changeStage(providenciaResponseDTO, evento);
    }

    // BOTON REPRESENTA PARA FLUJO DE SANCION NO APELA
    @Transactional
    public void noApela(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton NO APELA paso: ");
        AccionesProvidencia evento = AccionesProvidencia.FLUJO_NO_APELA;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void tomaRazon(ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton tomaRazon paso: ");
        AccionesProvidencia evento = null;
//        AccionesProvidencia evento = AccionesProvidencia.FLUJO_TOMA_RAZON;
//        if (providenciaResponseDTO.getSubEtapa().toString() == "RECEPCION_CONTRALORIA_TOMA_DE_RAZON_")
        if (providenciaResponseDTO.getRequisito()== EstadoProvidencia.RECEPCION_CONTRALORIA_TOMA_DE_RAZON_){
//            AccionesProvidencia evento = AccionesProvidencia.RECEPCION_CONTRALORIA_TOMA_DE_RAZON_;
            evento =  AccionesProvidencia.RECEPCION_CONTRALORIA_TOMA_DE_RAZON_;
        } else {
            evento = AccionesProvidencia.TOMA_DE_RAZON___;
        }
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void registra (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton registra paso: ");
        AccionesProvidencia evento = null;
        if (providenciaResponseDTO.getRequisito()== EstadoProvidencia.RECEPCION_CONTRALORIA_REGISTRA_) {
//        AccionesProvidencia evento = AccionesProvidencia.FLUJO_REGISTRA;
            evento = AccionesProvidencia.RECEPCION_CONTRALORIA_REGISTRA_;
        } else {
            evento = AccionesProvidencia.REGISTRA__;
        }
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void representa (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton representa paso: ");
        AccionesProvidencia evento = null;
        if (providenciaResponseDTO.getRequisito()== EstadoProvidencia.RECEPCION_CONTRALORIA_REPRESENTA_) {
//        AccionesProvidencia evento = AccionesProvidencia.FLUJO_REPRESENTA;
            evento = AccionesProvidencia.RECEPCION_CONTRALORIA_REPRESENTA_;
        } else {
            evento = AccionesProvidencia.REPRESENTA__;
        }
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void memoConductor (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton memoConductor paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();
        providencia.setRequisito(EstadoProvidencia.REALIZADO_MEMO_CONDUCTOR);
        providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
    }

    @Transactional
    public void examenLegalidad (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton examenLegalidad paso: ");
        AccionesProvidencia evento = AccionesProvidencia.SELECCIONO_EXAMEN_LEGALIDAD;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void emiteProvidencia (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton emiteProvidencia paso: ");
        AccionesProvidencia evento = AccionesProvidencia.EMITIR_PROVIDENCIA;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void alcance (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton alcance paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();
        providencia.setRequisito(EstadoProvidencia.SELECCION_ALCANCE);
        providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
    }

    @Transactional
    public void alcanceConResolucion (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton alcanceConResolucion paso: ");
        AccionesProvidencia evento = AccionesProvidencia.SELECCIONO_ALCANCE_CON_RESOLUCION;
        this.changeStage(providenciaResponseDTO, evento);
    }
    @Transactional
    public void alcanceSinResolucion (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton alcanceSinResolucion paso: ");
        AccionesProvidencia evento = AccionesProvidencia.SELECCIONO_ALCANCE_SIN_RESOLUCION;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void resolucion (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton resolucion paso: ");
        AccionesProvidencia evento = AccionesProvidencia.SELECCIONO_RESOLUCION;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void notificarDGDP (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton notificarDGDP paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();

        if (providencia.getRequisito()== EstadoProvidencia.NOTIFICA_E_INFORMA) {
            AccionesProvidencia evento = AccionesProvidencia.INFORMA_A_DGDP;
            Grupo groupAnswer = this.determineGroupAnswer(providencia);
            providencia.setRequisito(EstadoProvidencia.SELECCION_NOTIFICA_E_INFORMA);
            providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
            this.registryNotificacion("pendiente por hacer " + providencia.getRequisito(), groupAnswer, evento);

        }else if(providencia.getRequisito()== EstadoProvidencia.NOTIFICA_E_INFORMA_SENSURA || providencia.getRequisito()== EstadoProvidencia.NOTIFICA_E_INFORMA_DESTITUCION){
            AccionesProvidencia evento = AccionesProvidencia.NOTIFICAR_A_DGDP;
            this.changeStage(providenciaResponseDTO, evento);
        }
    }

    @Transactional
    public void suspension (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton suspension paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();
        providencia.setRequisito(EstadoProvidencia.NOTIFICA_E_INFORMA_SUSPENSION);
        providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
    }

    @Transactional
    public void multa (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton multa paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();
        providencia.setRequisito(EstadoProvidencia.NOTIFICA_E_INFORMA_MULTA);
        providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
    }

    @Transactional
    public void sensura (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton sensura paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();
        providencia.setRequisito(EstadoProvidencia.NOTIFICA_E_INFORMA_SENSURA);
        providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
    }

    @Transactional
    public void destitucion (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton destitucion paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();
        providencia.setRequisito(EstadoProvidencia.NOTIFICA_E_INFORMA_DESTITUCION);
        providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), providencia.getEtapa()));
    }

    @Transactional
    public void notificaRemuneracion (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton notificaRemuneracion paso: ");
        AccionesProvidencia evento = AccionesProvidencia.NOTIFICO_A_REMUNERACION;
        this.changeStage(providenciaResponseDTO, evento);
    }

    @Transactional
    public void notificaDenunciante (ProvidenciaResponseDTO providenciaResponseDTO) {
        log.debug("boton notificaDenunciante paso: ");
        Providencia providencia = providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId()).get();
        AccionesProvidencia evento = AccionesProvidencia.NOTIFICA_DENUNCIANTE;
        Grupo groupAnswer = this.determineGroupAnswer(providencia);
        this.registryNotificacion("pendiente por hacer " + providencia.getRequisito(), groupAnswer, evento);
    }

    /**
     * Método que recibe una devolución.
     *
     * @param providenciaResponseDTO
     */
    @Transactional
    public void goBackwards(ProvidenciaResponseDTO providenciaResponseDTO) {
        AccionesProvidencia evento = AccionesProvidencia.FLUJO_DEVOLVER;
        this.changeStage(providenciaResponseDTO, evento);
    }

    /*
     Método que permite determinar el nuevo estado de la providencia y transmite la información necesaria a la clase
     Derivación y MovimientoProvidencia.
     */
    private void changeStage(ProvidenciaResponseDTO providenciaResponseDTO, AccionesProvidencia evento) {
        Optional<Providencia> providenciaOptional = this.providenciaRepository.findById(providenciaResponseDTO.getProvidenciaId());
        Providencia providencia = null;
        ProvidenciaDTO providenciaDTO = null;
        EstadoProvidencia requisitoDespues = null;
        EstadoProvidencia subEtapa = null;
        EstadoProvidencia subEtapaAntes = null;
        EstadoProvidencia etapa = null;
        Long iDProvidenciaMadre = null;
        Long numeroDgd = null;
        Long numeroDgdp = null;
        log.debug("ruben: estado al entrar: " + requisitoDespues);
        if (providenciaOptional.isPresent()) {
            AccionesProvidencia eventoBoton = null;
            providencia = providenciaOptional.get();
//            if (evento == AccionesProvidencia.FISCAL_ACOGE)
//            {
//                etapa = EstadoProvidencia.ACOGE_INHABILIDAD;
//            } else if (evento == AccionesProvidencia.FISCAL_NO_ACOGE){
//                etapa = EstadoProvidencia.RECHAZA_INHABILIDAD;
//            }else{

                etapa = providencia.getEtapa();
//            }
//            etapa = providencia.getEtapa();

            //DEPENDE DE LA ETAPA LA ACCION CAMBIA PARA QUE LA PROVIDENCIA SIGA SU CAMINO CORRECTO
            log.debug("antes de entrar al metodo determianr evento REQUISITO DE LA PROVI " + providencia);
            log.debug("antes de entrar al metodo determianr evento " + evento);
            eventoBoton = determinarEvento(providencia, evento);

            if (eventoBoton == null) {
                eventoBoton = evento;
            }
            log.debug("saliendo del metodo determinar evento " + eventoBoton);
            subEtapaAntes = providencia.getSubEtapa();
            providencia.setRequisito(this.newState(providencia, eventoBoton)); // AQUI ES DONDE SE LLAMA A LA MAQUINA
            requisitoDespues = providencia.getRequisito();
            log.debug(" requisto nuevo despues del cambio de estado "+ requisitoDespues);

            subEtapa = getNextSubEtapa(subEtapaAntes, requisitoDespues, etapa, eventoBoton);
            log.debug("el valor de la sub eTapa es"+ subEtapa);
            providencia.setSubEtapa(subEtapa);

            /**
             * determinar etapara  ebn el chambio de estado verificar que no afecte el flujo
             * RubenEtapaNUEV
             */
            etapa = this.determinaEtapaenFlujo(subEtapa,requisitoDespues,providencia.getEtapa());
            log.debug("Ruben- set Etapa nueva: " +etapa);
            providencia.setEtapa(etapa);
            log.debug("Ruben- get Etapa nueva: "+providencia.getEtapa());
            subEtapa = providencia.getSubEtapa();
            iDProvidenciaMadre = providencia.getProvidencia_madre_id();
            providencia.setEstadoActual(this.concatenarEstado(requisitoDespues, subEtapa, etapa));

            /**
             * stanby actualiza el booleano en la db
             */
            standby(providencia);
            providenciaDTO = this.update(this.providenciaMapper.toDto(providencia), providenciaResponseDTO.getAdjuntosDTOs());
            Grupo groupAnswer = this.determineGroupAnswer(providencia);
            log.debug("despues del grupo " + providencia.getProvidencia_madre_id());
            Derivacion derivacion = null;

            //crea la notificacion
            this.registryNotificacion("pendiente por hacer " + requisitoDespues, groupAnswer,evento);

            if (providencia.getNombreFiscalAsignado() != null) {
                Optional<User> userOptional = this.userService.findByFullName(providencia.getNombreFiscalAsignado());

                if (userOptional.isPresent()) {
                    derivacion = this.registerDerivation(providenciaResponseDTO.getObservacion(),
                        this.providenciaMapper.toEntity(providenciaDTO), userOptional.get(), groupAnswer);
                }
            }

            derivacion = this.registerDerivation(providenciaResponseDTO.getObservacion(),
                this.providenciaMapper.toEntity(providenciaDTO), null, groupAnswer);
            String accion = "";

            if (this.userService.getCurrentUser().getGrupo().getId() == 1 || this.userService.getCurrentUser().getGrupo().getId() == 2) {
                accion = "Derivar";
            } else {
                accion = "Enviar";
            }

            log.info("aquinumeroDgd");
            numeroDgd = providencia.getNumeroDgd();
            numeroDgdp = providencia.getNumeroDgdp();
            this.movimientoProvidenciaService.save(providenciaResponseDTO.getEstadoActual(), providenciaDTO.getEstadoActual(),
                providencia.getId(), derivacion.getObservacion(), providenciaResponseDTO.getDocumentosDTOs(),
                providenciaResponseDTO.getAdjuntosDTOs(), accion, numeroDgd, numeroDgdp );
        }
        providencia.setProvidencia_madre_id(iDProvidenciaMadre);
        this.calcularPlazos(providencia);
        log.debug("Salida viendo cambio de id: " + providencia.getProvidencia_madre_id());
    }

    private void standby(Providencia providencia) {
        EstadoProvidencia requisito = providencia.getRequisito();

        if (requisito == EstadoProvidencia.DEMANDADO_NOTIFICADO){
            providencia.setStandby(true);
        }
    }

    // Metodo para determinar la subEtapa de una providencia
    private EstadoProvidencia getNextSubEtapa(EstadoProvidencia subEtapaAntes,EstadoProvidencia requisitoDespues,
                                              EstadoProvidencia etapa, AccionesProvidencia eventoBoton) {
        EstadoProvidencia nextSubEtapa;
        log.debug("entro en nextSubEtapa" + requisitoDespues + " | " + subEtapaAntes );

        if(requisitoDespues == EstadoProvidencia.FISCAL_REMITE_SUMARIO_A_DN){
            nextSubEtapa = subEtapaSegunSeleccion(eventoBoton);
        }else if (subEtapaAntes==EstadoProvidencia.SOBRESEER ){
            nextSubEtapa = subEtapaAntes;
        }else if (subEtapaAntes==EstadoProvidencia.ABSOLVER ){
            nextSubEtapa = subEtapaAntes;
        }else if (subEtapaAntes==EstadoProvidencia.SANCIONA ){
            if (requisitoDespues == EstadoProvidencia.TOMA_DE_RAZON || requisitoDespues == EstadoProvidencia.REGISTRA ||
                requisitoDespues == EstadoProvidencia.MEMO || requisitoDespues == EstadoProvidencia.SJ_RECIBIO_ALCANCE ||
                requisitoDespues == EstadoProvidencia.SJ_RECIBIO_RESOLUCION) {
                nextSubEtapa = subEtapaSegunSeleccion(eventoBoton);
            }else {
                nextSubEtapa = subEtapaAntes;
            }
        }else if (subEtapaAntes == EstadoProvidencia.SANCIONA_REGISTRA ||subEtapaAntes == EstadoProvidencia.SANCIONA_REPRESENTA ||
            subEtapaAntes == EstadoProvidencia.SANCIONA_TOMA_DE_RAZON || subEtapaAntes == EstadoProvidencia.SANCIONA_RESOLUCION ||
            subEtapaAntes == EstadoProvidencia.SANCIONA_ALCANCE){
            nextSubEtapa = subEtapaAntes;
        }else{
            nextSubEtapa = this.determinaSubEtapa(requisitoDespues, etapa);
        }
        return nextSubEtapa;
    }

    // Metodo para poder Setear la subEtapa para FiscalRemiteExpediente Segun la opcion Seleccionada (Termino Probatorio o Formula Cargos)
    private EstadoProvidencia subEtapaSegunSeleccion(AccionesProvidencia eventoBoton) {
        log.debug("entro a sub etapa segun seleccion" + eventoBoton);

        EstadoProvidencia subEtapa = null;

        if (eventoBoton == AccionesProvidencia.FORMULAR_CARGOS){
            subEtapa = EstadoProvidencia.FORMULA_CARGOS;
        }else if (eventoBoton == AccionesProvidencia.FLUJO_TOMA_RAZON){
            subEtapa = EstadoProvidencia.SANCIONA_TOMA_DE_RAZON;
        }else if (eventoBoton == AccionesProvidencia.FLUJO_REGISTRA){
            subEtapa = EstadoProvidencia.SANCIONA_REGISTRA;
        }else if (eventoBoton == AccionesProvidencia.FLUJO_REPRESENTA){
            subEtapa = EstadoProvidencia.SANCIONA_REPRESENTA;
        }else if (eventoBoton == AccionesProvidencia.SELECCIONO_RESOLUCION){
            subEtapa = EstadoProvidencia.SANCIONA_RESOLUCION;
        }else if (eventoBoton == AccionesProvidencia.SELECCIONO_ALCANCE_CON_RESOLUCION){
            subEtapa = EstadoProvidencia.SANCIONA_ALCANCE;
        }else if (eventoBoton == AccionesProvidencia.ENVIA_VISTA_FISCAL){
            subEtapa = EstadoProvidencia.ENVIA_VISTA_FISCAL_A_DN___;
        }else if (eventoBoton == AccionesProvidencia.FISCAL_ACOGE){
            subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL_____;
        }else if (eventoBoton == AccionesProvidencia.FISCAL_NO_ACOGE){
            subEtapa = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL____;
        }else {
            subEtapa = EstadoProvidencia.TERMINO_PROBATORIO;
        }
        return subEtapa;
    }

    // METODO QUE DA UNA ACCION PARA SEGUIR EL FLUJO SEGUN LA ETAPA DE LA PROVIDENCIA
    private AccionesProvidencia determinarEvento (Providencia providencia, AccionesProvidencia eventoAntes){

        EstadoProvidencia requisitoAntes = providencia.getRequisito();
        EstadoProvidencia subEtapaAntes = providencia.getSubEtapa();
        EstadoProvidencia etapa = providencia.getEtapa();
        AccionesProvidencia evento = null;
        EstadoProvidencia requisitoMadre = null;
        log.debug("entro a determinar evento CON EL ESTADO COMPLETO"+ requisitoAntes + subEtapaAntes + etapa);


        switch (requisitoAntes) {

            //Requisitos COMPARTIDO
            case DGD_RECEPCIONA:
            case SECRETARIA_REVISA_NOTIFICACION:
            case SECRETARIA_REVISA_FIRMA_NOTIFICACION:
                switch (etapa) {

                    case PROVIDENCIA_PRORROGA_1:
                        if(eventoAntes== AccionesProvidencia.FLUJO_DEVOLVER){
                            evento = AccionesProvidencia.FLUJO_DEVOLVER_PRORROGA1;
                        }else {
                            evento = AccionesProvidencia.PRORROGA;
                        }break;

                    case PROVIDENCIA_PRORROGA_2:
                        if(eventoAntes== AccionesProvidencia.FLUJO_DEVOLVER){
                            evento = AccionesProvidencia.FLUJO_DEVOLVER_PRORROGA2;
                        }else {
                            evento = AccionesProvidencia.PRORROGA2;
                        }break;

                    case PROVIDENCIA_CREADA:
                        evento = AccionesProvidencia.CREAR_PROVIDENCIA;
                        break;

                    case ACOGE_INHABILIDAD:
                        evento = AccionesProvidencia.FISCAL_ACOGE;
                        break;

                    case RECHAZA_INHABILIDAD:
                        evento = AccionesProvidencia.FISCAL_NO_ACOGE;
                        break;


                }
                break;

            case PROVIDENCIA_CREADA:
            case REVISA_PROVIDENCIA___:
//            case SJ_RECIBE_PROVIDENCIA:
//            case SECRETARIA_REVISA_PROVIDENCIA:
            case REVISA_PROVIDENCIA____:
            case ASIGNACION_No_DE_INGRESO:
            case PROVIDENCIA_APLICA_SANCION:
            case REVISA_PROVIDENCIA______:
            case ASIGNACION_DE_NUMERO_:
            case REVISA_SECRETARIA:
            case ASIGNACION_UPD:
            case REVISA_SECRETARIA_:
            case RESOLUCION_QUE_APLICA_SANCION_PARA_RECURSO_Y_MEMO_CONDUCTOR:
            case REVISA_SECRETARIA__:
            case VISA_RESOLUCION_Y_FIRMA_MEMO:
            case REVISA_SECRETARIA___:
            case ASIGNACION_No_DESPACHO_:
            case REVISA_ASESOR:
            case FIRMA_RESOLUCION_QUE_APLICA_SANCION_PARA_RECURSO:
            case REVISA_ASESOR_:
            case ASIGNA_No_DGDP:
            case RECEPCIONA_DGDP:
            case NOTIFICACION_PARA_RECURSO:
            case REVISA_SECRETARIA____:
            case FIRMA_SUB_DIRECTORA:
            case REVISA_SECRETARIA_____:
            case DESPACHO_DGD:
            case NOTIFICA_RESOLUCION_PARA_RECURSO:
//            case REVISA_PRONUNCIAMIENTO:
//            case ASIGNA_No_DGD:
//            case REVISA_SECRETARIA_______:
//            case REVISA_SECRETARIA________:
//            case REVISA_SECRETARIA_________:
//            case REVISA_SECRETARIA__________:
//            case REVISA_SECRETARIA___________:
//            case REVISA_SECRETARIA____________:
//            case ASIGNACION_SUB_DIRECTORA:
//            case RESOLUCION_QUE_PRONUNCIA_RECURSO_CON_MEMO_CONDUCTOR:
//            case VISA_Y_FIRMA_:
//            case DESPACHO_:
//            case REVISA_RESOLUCION_QUE_PRONUNCIA:
//            case FIRMA_DIRECTOR_NACIONAL_:
//            case DESPACHA_ASESOR:
//            case ASIGNACION_DE_NUMERO__:
//            case ASIGNA_NUMERO_SJ_:
//            case NOTIFICACION_RESULTADO_DE_RECURSO:
//            case ELABORA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
//            case VISA_Y_FIRMA__:
//            case DESPACHO_DE_SJ:
//            case REVISA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
//            case FIRMA_DIRECTOR_NACIONAL__:
//            case DESPACHA_ASESOR_:
//            case ASIGNACION_DE_NUMERO___:
//            case RECEPCION_CONTRALORIA:
//            case RECEPCION_CONTRALORIA_TOMA_DE_RAZON_:
//            case RECEPCION_CONTRALORIA_REGISTRA_:
//            case RECEPCION_CONTRALORIA_REPRESENTA_:
            case NUEVA_PROVIDENCIA__:
//            case APELA_NO_APELA:
//            case PROVIDENCIA_CREADA_INFORME_RECURSO:
            case NUEVA_PROVIDENCIA_ORDENANDO_SOBRESEDIMIENTO:
                switch (subEtapaAntes) {
                    case ABSOLVER:
                        evento = AccionesProvidencia.FLUJO_SOBRESEER_ABSOLVER_;
                        break;
                    case SOBRESEER:
                        evento = AccionesProvidencia.FLUJO_SOBRESEER_ABSOLVER;
                        break;
                    case SANCIONA:
                        evento = AccionesProvidencia.FLUJO_SANCION;
                        break;
                }
                break;
//            case NUEVA_PROVIDENCIA_ORDENANDO_SOBRESEDIMIENTO_:
//                switch (subEtapaAntes) {
//                    case ABSOLVER:
//                        evento = AccionesProvidencia.FLUJO_SOBRESEER_ABSOLVER_;
//                        break;
//                    case SOBRESEER:
//                        evento = AccionesProvidencia.FLUJO_SOBRESEER_ABSOLVER;
//                        break;
//                    case SANCIONA:
//                        evento = AccionesProvidencia.FLUJO_SANCION;
//                        break;
//                }
//                break;


            // FLUJO DE PRORROGA 1
            case UPD_ELABORA_NOTIFICACION_PRORROGA_1:
            case UPD_NOTIFICA_PRORROGA_1_FISCAL:
            case DGD_DESPACHA_NOTIFICACION_PRORROGA_1_FISCAL:
            case UPD_ELABORA_NOTIFICACION_PRORROGA_2:
            case DGD_DESPACHA_NOTIFICACION_PRORROGA_2_FISCAL:
                evento = AccionesProvidencia.PRORROGA;
                break;
            case REVISION_DE_PROVIDENCIA___:
            case ASIGNACION_DE_NUMERO_INGRESO___:
            case REVISION_DE_PROVIDENCIA____:
            case ASIGNACION_A_UPD__:
            case DERIVA_ASIGNACION__:
            case RESOLUCION_CONCEDE_PRORROGA_2:
            case REVISION_DE_RESOLUCION_Y_MEMO__:
            case FIRMA_MEMO_VISA_RESOLUCION__:
            case ENVIO_FIRMA_RESOLUCION_MEMO__:
            case ASIGNACION_No_DESPACHO__:
            case REVISA_RESOLUCION_Y_MEMO__:
            case FIRMA_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_2:
            case ENVIO_A_DGDP__:
            case ASIGNACION_No_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_2:
            case ASIGNACION_DE_NUMERO_INGRESO____:
            case FIRMA_PROVIDENCIA__:
            case COPIA_PRORROGA_2:
            case REVISION_NOTIFICACION_FISCAL_:
            case FIRMA_COPIA_PRORROGA_2:
            case ENVIO_A_DESPACHO__:
            case DGD_ASIGNA_No_DESPACHO___:
            case NOTIFICA_COPIA_PRORROGA_2:
            case INVESTIGACION__:
            case SOLICITUD_PRORROGA_2:
                evento = AccionesProvidencia.INVESTIGACION_;
                break;
//            case FISCAL_NOTIFICADO:
//                evento = AccionesProvidencia.CREAR_PROVIDENCIA;
//                break;
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION_PRORROGA_1:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION_PRORROGA_2:
                if (eventoAntes == AccionesProvidencia.FLUJO_DEVOLVER){
                    evento = eventoAntes;
                }else {
                    evento = AccionesProvidencia.PRORROGA;
                }break;

            // FLUJO FISCAL ACOGE / NO ACOGE -----------

            case ACOGE_INHABILIDAD:
            case NUEVA_PROVIDENCIA_DIRECCION_NACIONAL_____:
            case FIRMA_PROVIDENCIA___:
            case ASIGNACION_DE_NUMERO_INGRESO_____:
            case REVISA_PROVIDENCIA_______:
            case ASIGNACION_A_UPD___:
            case DERIVA_ASIGNACION____:
            case RESOLUCION_QUE_ACOGE_INHABILIDAD:
            case REVISION_DE_RESOLUCION_Y_MEMO___:
            case FIRMA_MEMO_VISA_RESOLUCION___:
            case ENVIO_FIRMA_RESOLUCION_MEMO___:
            case ASIGNACION_No_DESPACHO________:
            case REVISA_RESOLUCION_Y_MEMO___:
            case RESOLUCION_QUE_ACOGE_INHABILIDAD_:
            case ENVIO_A_DGDP___:
            case ASIGNACION_No_RESOLUCION:
            case ASIGNACION_No_DE_INGRESO_:
            case REDACION_NOTIFICACION_FISCAL:
            case REVISION_NOTIFICACION_FISCAL__:
            case FIRMA_NOTIFICACION_FISCAL_:
            case ENVIO_A_DESPACHO___:
            case ASIGNACION_No_DESPACHO_________:
            case NOTIFICACION_FISCAL____:
//            case FISCAL_NOTIFICADO:
//            case FISCAL_NOTIFICADO:
                evento = AccionesProvidencia.FISCAL_ACOGE;
                break;
//            case FISCAL_RECHAZO:
//                    evento = AccionesProvidencia.FISCAL_RECHAZA;
//                break;
            case RECHAZA_INHABILIDAD:
            case NUEVA_PROVIDENCIA_DIRECCION_NACIONAL____:
            case FIRMA_PROVIDENCIA____:
            case ASIGNACION_DE_NUMERO_INGRESO______:
            case REVISA_PROVIDENCIA________:
            case ASIGNACION_A_UPD____:
            case DERIVA_ASIGNACION_____:
            case RESOLUCION_QUE_RECHAZA_INHABILIDAD:
            case REVISION_DE_RESOLUCION_Y_MEMO____:
            case FIRMA_MEMO_VISA_RESOLUCION____:
            case ENVIO_FIRMA_RESOLUCION_MEMO____:
            case ASIGNACION_No_DESPACHO______:
            case REVISA_RESOLUCION_Y_MEMO____:
            case RESOLUCION_QUE_RECHAZA_INHABILIDAD_:
            case ENVIO_A_DGDP____:
            case ASIGNACION_No_RESOLUCION_:
            case ASIGNACION_No_DE_INGRESO__:
            case REDACION_NOTIFICACION_FISCAL_:
            case REVISION_NOTIFICACION_FISCAL___:
            case FIRMA_NOTIFICACION_FISCAL__:
            case ENVIO_A_DESPACHO____:
            case ASIGNACION_No_DESPACHO_______:
            case NOTIFICACION_FISCAL_____:
                evento = AccionesProvidencia.FISCAL_NO_ACOGE;
                break;
            // ---------------------------------------

            // FLUJO DE PRORROGA 2
            case UPD_NOTIFICA_PRORROGA_2_FISCAL:
                evento = AccionesProvidencia.PRORROGA2;
                break;

            // Para que el flujo salte al requisito PETICION PRORROGAS
            case INVESTIGACION:
                if(eventoAntes == AccionesProvidencia.FISCAL_NOTIFICA_A_UPD_CIERRE){
                    evento = AccionesProvidencia.FISCAL_NOTIFICA_A_UPD_CIERRE;
                }
                else if (providencia.getProvidencia_madre_id() == null){
                    evento = AccionesProvidencia.PRORROGA;
                }
                else if (providenciaRepository.findRequisitoByIdMadre(providencia.getProvidencia_madre_id())
                    == EstadoProvidencia.FISCAL_RECHAZO){
                    evento = AccionesProvidencia.PRORROGA;
                }else{
                    evento = AccionesProvidencia.PRORROGA2;
                }
                break;
//
            case INVESTIGACION_:
                evento = AccionesProvidencia.INVESTIGACION_;
                break;
//            case FISCAL_RECHAZO:
//                if(eventoAntes == AccionesProvidencia.FISCAL_NOTIFICA_A_UPD_CIERRE){
//                    evento = AccionesProvidencia.FISCAL_NOTIFICA_A_UPD_CIERRE;
//                }
//                else if (providencia.getProvidencia_madre_id() == null){
//                    evento = AccionesProvidencia.PRORROGA;
//                }
//                else if (providenciaRepository.findRequisitoByIdMadre(providencia.getProvidencia_madre_id())
//                    == EstadoProvidencia.FISCAL_RECHAZO){
//                    evento = AccionesProvidencia.PRORROGA;
//                }else{
//                    evento = AccionesProvidencia.PRORROGA2;
//                }
//                break;

            case SUBDIRECCION_ASIGNA_UPD_RESOLUCION_MEMO:
            case SECRETARIA_REVISA_ASIGNACION_UPD:
            case REDACCION_RESOLUCION_MEMO:
            case SECRETARIA_REVISA_RESOLUCION_MEMO:
            case ESPERANDO_FIRMA_VISA_DE_SUBDIRECCION_A_RESOLUCION:
            case SECRETARIA_REVISA_FIRMA_VISA_SUBDIRECCION:
            case DGD_DESPACHA_RESOLUCION:
            case ESPERANDO_FIRMA_DEL_DN_A_RESOLUCION:
            case DGDP_ASIGNANDO_NUMERO_A_RESOLUCION:
            case SJ_RECIBE_RESOLUCION:
            case UPD_ELABORA_NOTIFICACION_RESOLUCION:
            case SECRETARIA_REVISA_NOTIFICACION_RESOLUCION:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_RESOLUCION:
            case REVISA_PRONUNCIAMIENTO:
            case ASIGNA_No_DGD:
            case REVISA_SECRETARIA_______:
            case REVISA_SECRETARIA________:
            case REVISA_SECRETARIA_________:
            case REVISA_SECRETARIA__________:
            case REVISA_SECRETARIA___________:
            case REVISA_SECRETARIA____________:
            case ASIGNACION_SUB_DIRECTORA:
            case RESOLUCION_QUE_PRONUNCIA_RECURSO_CON_MEMO_CONDUCTOR:
            case VISA_Y_FIRMA_:
            case DESPACHO_:
            case REVISA_RESOLUCION_QUE_PRONUNCIA:
            case FIRMA_DIRECTOR_NACIONAL_:
            case DESPACHA_ASESOR:
            case ASIGNACION_DE_NUMERO__:
            case ASIGNA_NUMERO_SJ_:
            case NOTIFICACION_RESULTADO_DE_RECURSO:
            case ELABORA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
            case VISA_Y_FIRMA__:
            case DESPACHO_DE_SJ:
            case REVISA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
            case FIRMA_DIRECTOR_NACIONAL__:
            case DESPACHA_ASESOR_:
            case ASIGNACION_DE_NUMERO___:
            case RECEPCION_CONTRALORIA:
            case RECEPCION_CONTRALORIA_TOMA_DE_RAZON_:
            case RECEPCION_CONTRALORIA_REGISTRA_:
            case RECEPCION_CONTRALORIA_REPRESENTA_:
            case RECEPCIONA_SJ:
            case ELABORA_NOTIFICACION:
            case RECIBE_DGDP:
            case SECRETARIA_REVISA____:
            case SECRETARIA_REVISA___:
            case VISA_Y_FIRMA___:
            case DESPACHO_SJ:
            case NOTIFICACION_DENUNCIADO:
            case ELABORA_MEMO_Y_REMUNERACION:
            case ELABORA_MEMO_DGDP:
            case ASIGNA_FOLIO:
//            case NUEVA_PROVIDENCIA__:
            case SECRETARIA_REVISA_FIRMA_DE_SUBDIRECCION_A_RESOLUCION:
                if (eventoAntes == AccionesProvidencia.FLUJO_DEVOLVER){
                    evento = eventoAntes;
                }else {
                    evento = AccionesProvidencia.FLUJO_SANCION;
                }break;


            case CERTIFICACION_NO_APELA:
            case REDACCION_RESOLUCION_MEMO_NO_APELA:
            case REVISION_RESOLUCION_MEMO_NO_APELA:
            case ESPERANDO_FIRMA_Y_VISA_DE_SUBDIRECCION:
            case REVISANDO_FIRMA_Y_VISA_DE_SUBDIRECCION:
            case GESTOR_DOCUMENTAL_DESPACHA_A_DN:
            case DN_FIRMA_RESOLUCION_NO_APELA:
            case DGDP_ASIGNANDO_NUMERO_A_RESOLUCION_NO_APELA:
            case REALIZA_CERTIFICACION:
            case ELABORA_RESOLUCION_AFINATORIA_Y_MEMO:
            case REVISA_SECRETARIA_____________:
            case VISA_Y_FIRMA____:
            case REVISA_SECRETARIA______________:
            case NUMERO_DESPACHO:
            case REVISA_RESOLUCION_AFINATORIA_APLICA_SANCION_FINAL:
            case FIRMA_DIRECTOR_NACIONAL___:
            case DESPACHA_ASESOR__:
            case ASIGNACION_DE_NUMERO____:
            case RECEPCIONA_CONTRALORIA:
            case TOMA_DE_RAZON___:
            case REGISTRA__:
            case REPRESENTA__:
            case RECIBE_DGDP__:
            case RECEPCIONA_SJ_:
            case ELABORA_NOTIFICACION_QUE_APLICA_SANCION_FINAL:
            case REVISA_SECRETARIA_______________:
            case VISA_Y_FIRMA_____:
            case REVISA_SECRETARIA_________________:
            case DESPACHO_SJ_:
            case NOTIFICA_DENUNCIADO:
            case ELABORA_MEMO_Y_REMUNERACION_:
            case ELABORA_MEMO_DGDP_:
            case ASIGNA_FOLIO_:
            case RECIBE_DGDP___:
            case RECEPCIONA_SJ__:
            case ELABORA_NOTIFICACION_:
            case SECRETARIA_REVISA_____:
            case VISA_Y_FIRMA______:
            case SECRETARIA_REVISA______:
            case DESPACHO_SJ__:
            case NOTIFICA_DENUNCIADO_:
            case ELABORA_MEMO_Y_REMUNERACION__:
            case ELABORA_MEMO_DGDP__:
            case ASIGNA_FOLIO__:
                if (eventoAntes == AccionesProvidencia.FLUJO_DEVOLVER){
                    evento = eventoAntes;
                }else {
                    evento = AccionesProvidencia.FLUJO_NO_APELA;
                }break;

            case UPD_REGISTRA_INCULPADO_SI_APELA:
            case DGD_DESPACHA_APELACION_A_DN:
            case APELACION_RECIBIDA:
            case SJ_RECEPCIONA_APELACION:
            case SECRETARIA_REVISA_APELACION:
            case ASIGNANDO_ABOGADO_A_APELACION:
            case ABOGADO_ELABORA_INFORME_APELACION:
            case SECRETARIA_REVISA_INFORME_APELACION:
            case SUBDIRECCION_REVISA_INFORME_APELACION:
            case SECRETARIA_DESPACHA_INFORME_APELACION_A_DGD:
            case DGD_DESPACHA_INFORME_APELACION_A_DN:
            case DN_EMITE_PROVIDENCIA:
            case PROVIDENCIA_EN_SJ:
            case REVISION_DE_PROVIDENCIA:
            case SUBDIRECCION_ASIGNA_REDACCION_RESOLUCION_MEMO:
            case SECRETARIA_REVISA_ASIGNACION_REDACCION:
            case REDACCION_RESOLUCION_MEMO_UPD:
            case SECRETARIA_REVISA_REDACCION:
            case VISA_Y_FIRMA_SUBDIRECCION:
            case SECRETARIA_REVISA_VISA_Y_FIRMA_SUBDIRECCION:
            case DESPACHA_A_DN:
            case DN_FIRMA_RESOLUCION:
            case DGDP_ASIGNA_NUMERO:
            case SJ_RECIBE_RESOLUCION_CON_NUMERO:
            case REDACCION_DE_RESOLUCION:
            case SECRETARIA_REVISA_RESOLUCION:
            case REALIZADO_MEMO_CONDUCTOR:
            case SECRETARIA_REVISA_VISA_FIRMA_MEMO_CONDUCTOR:
            case DESPACHO_MEMO_CONDUCTOR_A_DN:
            case FIRMA_DEL_DN_A_RESOLUCION:
            case ESPERANDO_ASIGNACION_DE_NUMERO:
            case SJ_RECIBIO_ALCANCE:
            case SJ_RECIBIO_RESOLUCION:
//            case APELA_NO_APELA:
            case APELA:
//            case PROVIDENCIA_CREADA_INFORME_RECURSO:
            case ASESOR_REVISA_PROVIDENCIA_APELACION:
            case RECIBE_RECURSO:
            case REVISA_SECRETARIA______:
            case ASIGNA_ABOGADO:
            case SECRETARIA_REVISA:
            case INFORME_PRONUNCIADO:
            case INFORME_PRONUNCIADO_ACOGE_PARCIAL:
//            case ACOGE:
            case INFORME_PRONUNCIADO_ACOGE:
            case INFORME_PRONUNCIADO_RECHAZA:
            case SECRETARIA_REVISA_:
            case FIRMA_INFORME_:
            case SECRETARIA_REVISA__:
            case DESPACHO:
                if (eventoAntes == AccionesProvidencia.FLUJO_DEVOLVER){
                    evento = eventoAntes;
                }else if(eventoAntes == AccionesProvidencia.EMITIR_PROVIDENCIA) { // Accion para que Salte de estado cuando el requisito es DN_EMITE_PROVIDENCIA
                    evento = eventoAntes;
                }else{
                    evento = AccionesProvidencia.FLUJO_APELA;
                }break;
            case EXAMEN_DE_LEGALIDAD:
                evento = AccionesProvidencia.SELECCIONO_EXAMEN_LEGALIDAD;
                break;
            case TOMA_DE_RAZON:
            case SJ_RECIBE_PARA_REDACCION:
            case UPD_REDACTA_RESOLUCION_MEMO:
            case REALIZAR_NOTIFICACIONES:
                evento = AccionesProvidencia.FLUJO_TOMA_RAZON;
                break;

            case REGISTRA:
                evento = AccionesProvidencia.FLUJO_REGISTRA;
                break;
        }

        // Evento para cuando sea un OrdenJuridico SOBRESEER
        switch (subEtapaAntes) {
            case SOBRESEER:
                if(requisitoAntes==EstadoProvidencia.REDACCION_NOTIFICACION_MEMO_DEMANDANTE){
                    evento = AccionesProvidencia.NOTIFICA;
                }else if(eventoAntes == AccionesProvidencia.FLUJO_DEVOLVER) {
                    evento = eventoAntes;
                }else{
                    evento = AccionesProvidencia.FLUJO_SOBRESEER_ABSOLVER_;
                }break;
            case ABSOLVER:
                if(requisitoAntes==EstadoProvidencia.REDACCION_NOTIFICACION_MEMO_DEMANDANTE){
                    evento = AccionesProvidencia.NOTIFICA;
                }else if(eventoAntes == AccionesProvidencia.FLUJO_DEVOLVER) {
                    evento = eventoAntes;
                }else{
                    evento = AccionesProvidencia.FLUJO_SOBRESEER_ABSOLVER;
                }break;

            case REABRIR:
                evento = AccionesProvidencia.FLUJO_REABRIR;
                break;
        }
        return evento;
    }

    /*
     Estos métodos devuelven el nuevo estado de la Providencia.
     */
    private EstadoProvidencia newState (Providencia providencia, AccionesProvidencia accion){

        Long idProvidencia = providencia.getId();
        EstadoProvidencia siguienteEstado = null;
        EstadoProvidencia requisitoActual = providencia.getRequisito();
        log.debug(" Requisito Actual " + requisitoActual);

        if (requisitoActual == null) {
            siguienteEstado = EstadoProvidencia.PROVIDENCIA_CREADA;
            log.debug(" Requisito Actual es null " + siguienteEstado);

        } else {
            providenciaStateMachineService.nextState(idProvidencia, accion, requisitoActual);
            siguienteEstado = EstadoProvidencia.valueOf(stateMachine.getState().getId().toString());
            log.debug(" Requisito Actual no es null " + siguienteEstado);
        }
        log.debug(" Antes del return ");
        return siguienteEstado;
    }

    /*
        Método que permite, en función del estado, determinar qué grupo o departamento debe hacerse cargo del flujo de
        una providencia.
     */
    private Grupo determineGroupAnswer (Providencia providencia){
        Grupo groupAnswer = null;
        Optional<Grupo> optionalGroup = null;
        EstadoProvidencia etapa = providencia.getEtapa();

        switch (etapa) {
            // Permiso para el grupo Dirección Nacional DN((Toda la Etapa 1 PRUEBA))
            case NUEVA_PROVIDENCIA:
                optionalGroup = this.grupoService.findOne(1L).map(this.grupoMapper::toEntity);
                if (optionalGroup.isPresent()) groupAnswer = optionalGroup.get();
                break;

            case PROVIDENCIA_SELECCION_FISCAL:
            case INVESTIGACION:
            case REVISION_SUMARIO:
            case INFORME_JURIDICO:
            case PROVIDENCIA_PRORROGA_1:
            case PROVIDENCIA_PRORROGA_2:
            case INVESTIGACION_PRORROGA_1:
            case INVESTIGACION_PRORROGA_2:
            case PROVIDENCIA_SIN_RESOLUCION:
            case ACOGE_INHABILIDAD:
            case RECHAZA_INHABILIDAD:
                optionalGroup = this.grupoService.findOne(1L).map(this.grupoMapper::toEntity);
                if (optionalGroup.isPresent()) groupAnswer = optionalGroup.get();
                break;
        }
        return groupAnswer;
    }

    /**
     * Método que permite saber qué acciones están permitidas en función del Requisito de la providencia.
     * Los permisos también están limitados por el perfil y grupo. Todas estás acciones se ven reflejadas solo en el detalle de la providencia.
     * Tambien permite la visualizacion de Botones segun Requisito
     *
     * @param providenciaDTO
     * @return
     */
    @Transactional(readOnly = true)
    public HashMap<String, Boolean> getActionsPermitted (ProvidenciaPermisoDTO providenciaDTO){

        EstadoProvidencia requisito = providenciaDTO.getRequisito();
        EstadoProvidencia etapa = providenciaDTO.getEtapa();
        EstadoProvidencia subEtapa= providenciaDTO.getSubEtapa();
        log.debug("ruben2: reci " +providenciaDTO.getId());

//
//        log.debug("ruben2: ultimo numero de dgd " +ultimoDgd);
        if (providenciaDTO.getEtapa() == null && providenciaDTO.getSubEtapa() == null && providenciaDTO.getRequisito() == null) {
            return null;
        }
        Grupo grupoCurrentUser = this.userService.getCurrentUser().getGrupo();
        Perfil perfilUser = this.userService.getCurrentUser().getPerfil();

        if (grupoCurrentUser == null) {
            return null;
        }
        HashMap<String, Boolean> actionsPermitted = new HashMap<>();
        // Se agregan las acciones con un valor false de inicio.
        // "reply" permite mostrar u ocultar el botón continuar para cambiar de estado.
        // "goBackwards" permite mostrar u ocultar el botón que da pie a volver o rechazar a la etapa anterior.
        // "watchTabRespuesta" permite mostrar u ocultar el tab que da pie a crear una respuesta.
        // "asignarFiscal" permite mostrar u ocultar el botón que da pie a abrir un model y asignar el nombre del fiscal.
        // "relacionarProvidencia" permite mostrar u ocultar el botón que da pie a relacionar una providencia.
        // "numerarReferencia" permite mostrar u ocultar el botón que da pie a asignar el número de referencia.
        // "tipoSolicitud" permite mostrar u ocultar el botón que da pie a asignar el tipo de solicitud.
        actionsPermitted.put("reply", false);
        actionsPermitted.put("fiscalNotificaCierre", false);
        actionsPermitted.put("fiscalNotificaUPD", false);
        actionsPermitted.put("goBackwards", false);
        actionsPermitted.put("watchTabRespuesta", true);
        actionsPermitted.put("asignarFiscal", false);
        actionsPermitted.put("relacionarProvidencia", false);
        actionsPermitted.put("numerarReferencia", false);
        actionsPermitted.put("folio", false);
        actionsPermitted.put("asignarNumeroDGD", false);
        actionsPermitted.put("asignarNumeroDGDP", false);
        actionsPermitted.put("cambiadoNumeroDGD", false);
        actionsPermitted.put("cambiadoNumeroDGDP", false);

        actionsPermitted.put("tipoSolicitud", false);
        actionsPermitted.put("aceptar", false);
        actionsPermitted.put("rechazar", false);
        actionsPermitted.put("prorroga", false);
        actionsPermitted.put("inculpadoEnviaMemo", false);
        actionsPermitted.put("inculpadoNoEnviaMemo", false);
        actionsPermitted.put("formularCargos", false);
        actionsPermitted.put("terminoProbatorio", false);
        actionsPermitted.put("siDeAcuerdo", false);
        actionsPermitted.put("noReabro", false);
        actionsPermitted.put("noPropone", false);
        actionsPermitted.put("remiteExpediente", false);
        actionsPermitted.put("apela", false);
        actionsPermitted.put("noApela", false);
        actionsPermitted.put("tomaRazon", false);
        actionsPermitted.put("registra", false);
        actionsPermitted.put("representa", false);
        actionsPermitted.put("memoConductor", false);
        actionsPermitted.put("examenLegalidad", false);
        actionsPermitted.put("alcance", false);
        actionsPermitted.put("resolucion", false);
        actionsPermitted.put("alcanceConResolucion", false);
        actionsPermitted.put("alcanceSinResolucion", false);
        actionsPermitted.put("suspension", false);
        actionsPermitted.put("multa", false);
        actionsPermitted.put("sensura", false);
        actionsPermitted.put("destitucion", false);
        actionsPermitted.put("notificarDGDP", false);
        actionsPermitted.put("notificaRemuneracion", false);
        actionsPermitted.put("notificaDemandado", false);
        actionsPermitted.put("notificaDenunciante", false);
        actionsPermitted.put("asignarAbogado", false);
        actionsPermitted.put("sinDenunciante", false);
        actionsPermitted.put("asignarNumeroIngreso", false);
        actionsPermitted.put("asignarNumeroDespacho", false);
        actionsPermitted.put("asignarAUpd", false);
        actionsPermitted.put("enviaVistaFiscal", false);
        actionsPermitted.put("acogeParcial", false);
        actionsPermitted.put("acoge", false);
        actionsPermitted.put("rechaza", false);
        actionsPermitted.put("acogeFiscal", false);
        actionsPermitted.put("noAcogeFiscal", false);

        switch (requisito) {

            case NUEVA_PROVIDENCIA:
            case NUEVA_PROVIDENCIA_:
            case PROVIDENCIA_CREADA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {

                    actionsPermitted.put("reply", true);

                    actionsPermitted.put("relacionarProvidencia", true);
                }
                break;
            case REVISION_PROVIDENCIA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);


                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case FIRMA_PROVIDENCIA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);


                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_NRO_INGRESO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarNumeroIngreso", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_PROVIDENCIA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);

                }
                break;
            case PROVIDENCIA_CREADA_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {

                    actionsPermitted.put("reply", false);

                }
                break;
            case FIRMA_PROVIDENCIA_:
            case FIRMA_PROVIDENCIA__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {

                    actionsPermitted.put("reply", true);

                }
                break;
//            case PROVIDENCIA_CREADA___:
//                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
//                    actionsPermitted.put("goBackwards", false);
//                    actionsPermitted.put("reply", true);
//                }
//                break;
            case REVISA_PROVIDENCIA_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_A_UPD:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", false);
                    actionsPermitted.put("reply", true);
                }
                break;
            case DERIVA_ASIGNACION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
//                    actionsPermitted.put("asignarFiscal", false);
                }
                break;
            case DERIVA_ASIGNACION___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
//                    actionsPermitted.put("asignarFiscal", false);
                }
                break;
            case RESOLUCION_QUE_INSTRUYE_SUMARIO_MEMO_CONDUCTOR:
//            case RESOLUCION_QUE_CONCEDE_PRORROGA_1:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("numerarReferencia", false);
                    actionsPermitted.put("relacionarProvidencia", false);
                    actionsPermitted.put("goBackwards", false);
                    actionsPermitted.put("asignarFiscal", false);
                }
                break;
            case REVISION_DE_RESOLUCION_Y_MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("numerarReferencia", true);
                    actionsPermitted.put("relacionarProvidencia", false);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", false);
                }
                break;
            case FIRMA_MEMO_VISA_RESOLUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("numerarReferencia", true);
                    actionsPermitted.put("relacionarProvidencia", false);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", false);
                }
                break;
            case ENVIO_FIRMA_RESOLUCION_MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", false);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_NRO_DESPACHO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarFiscal", false);
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_RESOLUCION_Y_MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", false);
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_RESOLUCION_Y_MEMO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", false);
                    actionsPermitted.put("reply", true);
                }
                break;
            case FIRMA_DE_RESOLUCION_QUE_INSTRUYE_SUMARIO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarFiscal", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ENVIO_A_DGDP:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", false);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_No_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_1:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_DE_NUMERO_INGRESO__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case COPIA_PRORROGA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", true);
                    actionsPermitted.put("reply", true);
                }
                break;

            case NOTIFICACION_FISCAL_SJ:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarNumeroIngreso", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_NRO_DE_RESOLUCION_QUE_INSTRUYE_SUMARIO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    // ESTE ES EL PRIMER NUMERO DGDP PERO EN BACK SE TRATA COMO NUMERO DE REFERENCIA
                    actionsPermitted.put("numerarReferencia", true);
                    Long ultimovimiento=  this.providenciaRepository.findNumberReferentForID(providenciaDTO.getId() );
                    if(null != ultimovimiento) {
                        actionsPermitted.put("reply", true);
                    }
                }
                break;
            case ASIGNACION_NRO_INGRESO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarNumeroIngreso", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case REDACCION_NOTIFICACION_FISCAL:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarFiscal", true);
                    actionsPermitted.put("goBackwards", false);
                    Boolean respuestaplantilla=  this.respuestaService.findByversihayplantillaadjunta(providenciaDTO.getId());
                    log.debug("ruben2: plantila " + respuestaplantilla);
                    actionsPermitted.put("reply", respuestaplantilla);
                }
                break;
            case REVISION_NOTIFICACION_FISCAL:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case FIRMA_NOTIFICACION_FISCAL:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ENVIO_A_DESPACHO:
            case ASIGNACION_NRO_DESPACHO_:
            case NOTIFICACION_FISCAL_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
                }
                break;
//            case FISCAL_NOTIFICADO:
//                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
//                    actionsPermitted.put("aceptar", true);
//                    actionsPermitted.put("rechazar", true);
//                    actionsPermitted.put("goBackwards", true);
//                } else {
//                    actionsPermitted.put("watchTabRespuesta", false);
//                }
//                break;
            case SECRETARIA_REVISA_NUMERO:
            case REVISA_PROVIDENCIA__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("numerarReferencia", true);
                    actionsPermitted.put("relacionarProvidencia", false);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case SECRETARIA_REVISA_ASIGNACION:
            case DERIVA_ASIGNACION_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("numerarReferencia", true);
                    actionsPermitted.put("relacionarProvidencia", false);
                    actionsPermitted.put("goBackwards", true);
                }
            case SUB_DIRECCION_DEBE_ASIGNAR:
            case ASIGNACION_A_UPD_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarAUpd ", true);
                }
                break;
            case UPD_REDACTA_RESOLUCION_Y_MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("numerarReferencia", true);

                }
                break;
            case RESOLUCION_CONCEDE_PRORROGA_1:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);

                    actionsPermitted.put("relacionarProvidencia", false);
                    actionsPermitted.put("tipoProvidencia", true);
                }
                break;
            case REVISA_PROVIDENCIA___:
            case SJ_RECIBE_PROVIDENCIA:
            case ASIGNACION_No_DE_INGRESO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case GESTOR_DOCUMENTAL_ASIGNA_NUMERO:
            case ASIGNACION_DE_NUMERO_INGRESO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGD", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case PROVIDENCIA_EN_SJ:
            case SJ_RECEPCIONA_APELACION:
            case DGD_DESPACHA_SUMARIO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {

//                    if(ultimoDgd != providenciaDTO.getNumeroDgd()) {
//                        log.debug("ruben3 "+ultimoDgd+ "es " +providenciaDTO.getNumeroDgd());
                    actionsPermitted.put("reply", true);
//                    }else{
//                        actionsPermitted.put("cambiadoNumeroDGD", true);
                    actionsPermitted.put("asignarNumeroDGD", true);
//                    }


                }
                break;
            case ASIGNACION_DE_NUMERO_INGRESO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarNumeroIngreso", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_SUMARIO_COMPLETO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_DE_ABOGADO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarAbogado", true);
                }
                break;
            case REVISA_ASIGNACION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ELABORACION_DE_INFORME_JURIDICO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("noReabro", true);
                    actionsPermitted.put("siDeAcuerdo", true);
                    actionsPermitted.put("noApela", true);
                    actionsPermitted.put("reply", false);
                }
                break;
            case NO_REABRE:
            case SI_DE_ACUERDO:
            case NO_PROPONE:
            case REVISA_INFORME:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case FIRMA_INFORME:
            case NOTIFICACION_DENUNCIADO:
            case ELABORA_MEMO_Y_REMUNERACION:
            case ELABORA_MEMO_DGDP:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNA_FOLIO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("folio", true);
                }
                break;
//            case TOMA_DE_RAZON____:
            case RECEPCIONA_SJ_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case ELABORA_NOTIFICACION_QUE_APLICA_SANCION_FINAL:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_SECRETARIA_______________:
            case VISA_Y_FIRMA_____:
            case REVISA_SECRETARIA_________________:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case DESPACHO_SJ_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case NOTIFICA_DENUNCIADO:
            case ELABORA_MEMO_Y_REMUNERACION_:
            case ELABORA_MEMO_DGDP_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNA_FOLIO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("folio", true);
                }
                break;
            case ENVIA_INFORME_A_DESPACHO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case No_DESPACHO_SJ:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)   || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5))   {
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case DESPACHO_COMPLETO:
            case ESPERANDO_ASIGNACION_DE_NUMERO:

            case DGDP_ASIGNANDO_NUMERO_A_RESOLUCION_NO_APELA:
            case DGDP_ASIGNANDO_NUMERO_A_RESOLUCION:
            case ASIGNACION_DE_NUMERO:
            case DGDP_ASIGNANDO_NUMERO:
            case DGDP_ASIGNA_NUMERO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    Long ultimoDgdp=  this.movimientoProvidenciaService.findNumeroDGDPMasRecienteByProvidenciaId(providenciaDTO.getId());
                    log.debug("ruben2: ultimo numero de dgd " + ultimoDgdp);
                    if(ultimoDgdp != providenciaDTO.getNumeroDgdp()) {
                        actionsPermitted.put("reply", true);
                    }else{
                        actionsPermitted.put("asignarNumeroDGDP", true);
                        }

                }
                break;
            case REALIZA_CERTIFICACION:
            case ELABORA_RESOLUCION_AFINATORIA_Y_MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_SECRETARIA_____________:
            case VISA_Y_FIRMA____:
            case REVISA_SECRETARIA______________:
            case SECRETARIA_REVISA_FIRMA_NOTIFICACION:
            case REVISA_RESOLUCION_AFINATORIA_APLICA_SANCION_FINAL:
            case FIRMA_DIRECTOR_NACIONAL___:
            case DESPACHA_ASESOR__:
            case ENVIO_A_DGDP_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case RECEPCIONA_CONTRALORIA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("tomaRazon", true);
                    actionsPermitted.put("registra", true);
                    actionsPermitted.put("representa", true);
                }
                break;
            case TOMA_DE_RAZON___:
            case REGISTRA__:
            case REPRESENTA__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                }
                break;
            case NUMERO_DESPACHO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case ASIGNACION_DE_NUMERO____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case ENVIAR_A_SUB_DIRRECION_JURIDICA:
            case UPD_ELABORA_NOTIFICACION_VISTA_FISCAL:
            case DGD_DESPACHA_NOTIFICACION_FISCAL:
            case ENVIADO_A_SUBDIRECCION_JURIDICA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarFiscal", true);
                }
                break;
            case UPD_NOTIFICA_FISCAL:
            case DGD_RECEPCIONA:
            case DGD_DESPACHA_A_DN:
            case ASIGNACION_No_DESPACHO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case SECRETARIA_REVISA_FIRMA:
            case ENVIO_FIRMA_RESOLUCION_MEMO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case UPD_ELABORA_NOTIFICACION_PRORROGA_1:
            case UPD_NOTIFICA_PRORROGA_1_FISCAL:
            case DGD_DESPACHA_NOTIFICACION_PRORROGA_1_FISCAL:
            case UPD_ELABORA_NOTIFICACION_PRORROGA_2:
            case UPD_NOTIFICA_PRORROGA_2_FISCAL:
            case DGD_DESPACHA_NOTIFICACION_PRORROGA_2_FISCAL:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;

            case SECRETARIA_REVISA_RESOLUCION_Y_MEMO:
            case REVISION_DE_RESOLUCION_Y_MEMO_:
            case ESPERANDO_FIRMA_VISA_DE_SUBDIRECCION:
            case FIRMA_MEMO_VISA_RESOLUCION_:
            case ESPERANDO_FIRMA_DEL_DN:
            case FIRMA_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_1:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", true);
                }
                break;
//            case SI_DE_ACUERDO:
//            case NO_REABRE:
//            case NO_PROPONE:
            case SECRETARIA_REVISA_INFORME:
            case SUB_DIRECCION_REVISA_INFORME:
            case SECRETARIA_REVISA_NOTIFICACION:
            case REVISO_NOTIFICACION_FISCAL:
            case FIRMA_COPIA_PRORROGA_1:
            case ENVIO_A_DESPACHO_:
            case DGD_ASIGNACION_No_DESPACHO:
            case NOTIFICA_COPIA_PRORROGA_1:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION_PRORROGA_1:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_NOTIFICACION_PRORROGA_2:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case INVESTIGACION_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                    actionsPermitted.put("fiscalNotificaCierre", true);
                    if (etapa != EstadoProvidencia.INVESTIGACION_PRORROGA_2){
                        actionsPermitted.put("prorroga", true);
                    }
                }
                break;
            case INVESTIGACION_CERRADA:
                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){
                    if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                        actionsPermitted.put("formularCargos", true);
                        actionsPermitted.put("sinDenunciante", true);
                    } else {
                        actionsPermitted.put("watchTabRespuesta", false);
                    }
                    break;
                }
                break;
            case INVESTIGACION_CERRADA_:
                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){
                    if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                        actionsPermitted.put("formularCargos", true);
                        actionsPermitted.put("sinDenunciante", true);
                    } else {
                        actionsPermitted.put("watchTabRespuesta", false);
                    }
                    break;
                }
                break;
            case INCULPADO_NOTIFICADO:
//                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){
                if (subEtapa== EstadoProvidencia.FORMULA_CARGOS_FISCALIA){
                    if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                        actionsPermitted.put("inculpadoEnviaMemo", true);
                        actionsPermitted.put("inculpadoNoEnviaMemo",true);
                    } else {
                        actionsPermitted.put("watchTabRespuesta", false);
                    }
                    break;
                }
                break;
            case FORMULA_CARGOS_Y_NOTIFICA:
//                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){
                if (subEtapa== EstadoProvidencia.FORMULA_CARGOS_FISCALIA){
                    if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                        actionsPermitted.put("inculpadoEnviaMemo", true);
                        actionsPermitted.put("inculpadoNoEnviaMemo",true);
                    } else {
                        actionsPermitted.put("watchTabRespuesta", false);
                    }
                    break;
                }
                break;
//            case ENVIA_VISTA_FISCAL_A_DN:
//                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
//                    actionsPermitted.put("reply", false);
//                    actionsPermitted.put("terminoProbatorio", true);
//                }
//                break;
            case REALIZA_DESCARGO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("terminoProbatorio", true);
                    actionsPermitted.put("enviaVistaFiscal", true);
                }
                break;
//            case NUEVO_ESTADO:
            case ENVIA_VISTA_FISCAL_A_DN_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("terminoProbatorio", false);
                    actionsPermitted.put("tipoSolicitud", false);
                }
                break;
            case ENVIA_VISTA_FISCAL_A_DN__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("terminoProbatorio", false);
                    actionsPermitted.put("tipoSolicitud", false);
                }
                break;
            case ENVIA_VISTA_FISCAL_A_DN___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("terminoProbatorio", false);
                    actionsPermitted.put("tipoSolicitud", false);
                }
                break;
            case ASIGNACION_A_SJ:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISION_ASIGNACION_Y_ENVIA_SJ:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
//            case ASIGNACION_DE_NUMERO_INGRESO:
//                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
//                    actionsPermitted.put("asignarNumeroIngreso", true);
//                }
//                break;
            case REMITE_EXPEDIENTE_VISTA_FISCAL:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("tipoSolicitud", false);
                }
                break;
            case TERMINO_PROBATORIO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("terminoProbatorio", false);
                }
                break;
            case INCULPADO_ENVIA_MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("formularCargos", false);
                    actionsPermitted.put("reply", true);
                }
                break;
            case INCULPADO_NO_ENVIA_MEMO:
                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){

                    if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                        actionsPermitted.put("formularCargos", false);
                        actionsPermitted.put("terminoProbatorio", false);
                    } else {
                        actionsPermitted.put("watchTabRespuesta", false);
                    }
                    break;
                }
                break;


            case FORMULA_CARGOS_TERMINO_PROBATORIO:
            case FISCAL_REMITE_EXPEDIENTE:
                if (subEtapa== EstadoProvidencia.DA_INICIO_FISCALIA){

                    if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                        actionsPermitted.put("remiteExpediente", true);
                    }
                }
                break;

            case FISCAL_REMITE_SUMARIO_A_DN:
//            case ASIGNACION_A_SJ:
//                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
//                    actionsPermitted.put("goBackwards", true);
//                    actionsPermitted.put("reply", true);
//                }
//                break;
            case ENVIAR_SUMARIO_A_SUB_DIRECCION:
            case SECRETARIA_REVISA_SUMARIO:
//            case REVISA_SUMARIO_COMPLETO:
//                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
//                    actionsPermitted.put("goBackwards", true);
//                }
            case SECRETARIA_REVISA_SUMARIO_Y_NOTIFICA_A_ABOGADO:
            case SECRETARIA_DESPACHA_INFORME:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;

            case SECRETARIA_REVISA_PROVIDENCIA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_PROVIDENCIA____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case NUEVA_PROVIDENCIA_ORDENANDO_SOBRESEDIMIENTO:
            case NUEVA_PROVIDENCIA_ORDENANDO_SOBRESEDIMIENTO_:
            case PROVIDENCIA_APLICA_SANCION:
            case REVISA_PROVIDENCIA______:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_DE_NUMERO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case REVISA_SECRETARIA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_UPD:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarAUpd", true);
                }
                break;
            case REVISA_SECRETARIA_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case RESOLUCION_QUE_APLICA_SANCION_PARA_RECURSO_Y_MEMO_CONDUCTOR:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_SECRETARIA__:
            case VISA_RESOLUCION_Y_FIRMA_MEMO:
            case REVISA_SECRETARIA___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_No_DESPACHO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case REVISA_ASESOR:
            case FIRMA_RESOLUCION_QUE_APLICA_SANCION_PARA_RECURSO:
            case REVISA_ASESOR_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNA_No_DGDP:
            case RECEPCIONA_DGDP:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                }
                break;
            case NOTIFICACION_PARA_RECURSO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case REVISA_SECRETARIA____:
            case FIRMA_SUB_DIRECTORA:
            case REVISA_SECRETARIA_____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case DESPACHO_DGD:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("asignarNumeroDespacho", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case NOTIFICA_RESOLUCION_PARA_RECURSO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case APELA_NO_APELA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("apela", true);
                    actionsPermitted.put("noApela", true);
                    actionsPermitted.put("asignarAbogado", false);
                    actionsPermitted.put("reply", false);
                }
                break;
            case APELA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                }
                break;
            case NO_APELA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                }
                break;
//            case PROVIDENCIA_CREADA_INFORME_RECURSO:
            case ASESOR_REVISA_PROVIDENCIA_APELACION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case RECIBE_RECURSO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case REVISA_SECRETARIA______:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNA_ABOGADO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarAbogado", true);
                }
                break;
            case SECRETARIA_REVISA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case INFORME_PRONUNCIADO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("acogeParcial", true);
                    actionsPermitted.put("acoge", true);
                    actionsPermitted.put("rechaza", true);
                }
                break;
            case INFORME_PRONUNCIADO_ACOGE_PARCIAL:
//            case ACOGE:
            case INFORME_PRONUNCIADO_ACOGE:
            case INFORME_PRONUNCIADO_RECHAZA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case SECRETARIA_REVISA_:
            case FIRMA_INFORME_:
            case SECRETARIA_REVISA__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case DESPACHO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                }
                break;
            case REVISA_PRONUNCIAMIENTO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case RECIBE_DGDP_:
            case RECIBE_DGDP:
            case ASIGNA_No_DGD:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("asignarNumeroDGD", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case RECIBE_DGDP__:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case RECIBE_DGDP___:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                }
                break;
            case RECEPCIONA_SJ__:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case ELABORA_NOTIFICACION_:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case SECRETARIA_REVISA_____:
            case VISA_Y_FIRMA______:
            case SECRETARIA_REVISA______:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case DESPACHO_SJ__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case NOTIFICA_DENUNCIADO_:
            case ELABORA_MEMO_Y_REMUNERACION__:
            case ELABORA_MEMO_DGDP__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNA_FOLIO__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("folio", true);
                }
                break;
            case REVISA_SECRETARIA_______:
            case REVISA_SECRETARIA________:
            case REVISA_SECRETARIA_________:
            case REVISA_SECRETARIA__________:
            case REVISA_SECRETARIA___________:
            case REVISA_SECRETARIA____________:
            case ASIGNACION_SUB_DIRECTORA:
            case VISA_Y_FIRMA_:
            case REVISA_RESOLUCION_QUE_PRONUNCIA:
            case FIRMA_DIRECTOR_NACIONAL_:
            case DESPACHA_ASESOR:
            case VISA_Y_FIRMA__:
            case REVISA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
            case FIRMA_DIRECTOR_NACIONAL__:
            case DESPACHA_ASESOR_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case RECEPCION_CONTRALORIA:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("tomaRazon", true);
                    actionsPermitted.put("registra", true);
                    actionsPermitted.put("representa", true);
                }
                break;
            case RECEPCION_CONTRALORIA_TOMA_DE_RAZON_:
            case RECEPCION_CONTRALORIA_REGISTRA_:
            case RECEPCION_CONTRALORIA_REPRESENTA_:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", false);
                }
                break;
            case ELABORA_NOTIFICACION:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case SECRETARIA_REVISA____:
            case SECRETARIA_REVISA___:
            case VISA_Y_FIRMA___:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case DESPACHO_SJ:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case RECEPCIONA_SJ:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case PRONUNCIANDO_RECURSO_:
            case TOMA_DE_RAZON__:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_DE_NUMERO__:
            case ASIGNACION_DE_NUMERO___:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                }
                break;
            case NOTIFICACION_RESULTADO_DE_RECURSO:
            case ELABORA_RESOLUCION_QUE_APLICA_SANCION_FINAL:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNA_NUMERO_SJ_:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case RESOLUCION_QUE_PRONUNCIA_RECURSO_CON_MEMO_CONDUCTOR:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case DESPACHO_DE_SJ:
            case DESPACHO_:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case ASIGNANDO_ABOGADO_A_APELACION:
            case SUB_DIRECCION_ASIGNA_ABOGADO:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("asignarAbogado", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case SUB_DIRECCION_ASIGNA_A_RESOLUCION_Y_MEMO:
            case ASIGNA_A_UPD_PARA_RESOLUCION_QUE_SOBRESEE_SUMARIO_Y_MEMO:
            case ASIGNA_A_UPD_PARA_RESOLUCION_QUE_ABSUELVE_SUMARIO:
            case REDACCION_RESOLUCION_QUE_SOBRESEE_SUMARIO:
            case REDACCION_RESOLUCION_QUE_ABSUELVE_SUMARIO:
            case SECRETARIA_DESPACHA_A_UPD:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case SOLICITUD_PRORROGA_2:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", false);
                }
                break;
            case REVISION_DE_PROVIDENCIA___:
            case REVISION_DE_PROVIDENCIA____:
            case DERIVA_ASIGNACION__:
            case REVISION_DE_RESOLUCION_Y_MEMO__:
            case FIRMA_MEMO_VISA_RESOLUCION__:
            case ENVIO_FIRMA_RESOLUCION_MEMO__:
            case REVISA_RESOLUCION_Y_MEMO__:
            case ENVIO_A_DGDP__:
            case REVISION_NOTIFICACION_FISCAL_:
            case FIRMA_COPIA_PRORROGA_2:
            case ENVIO_A_DESPACHO__:
            case RECIBE_NOTIFICACION_FIRMADA:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case DGD_ASIGNA_No_DESPACHO___:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case NOTIFICA_COPIA_PRORROGA_2:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                }
                break;
            case INVESTIGACION__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                    actionsPermitted.put("fiscalNotificaCierre", true);
//                    if (etapa != EstadoProvidencia.INVESTIGACION_PRORROGA_2){
//                        actionsPermitted.put("prorroga", true);
//                    }
                }
                break;
            case ASIGNACION_DE_NUMERO_INGRESO____:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case ASIGNACION_No_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_2:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                }
                break;
            case FIRMA_DE_RESOLUCION_QUE_CONCEDE_PRORROGA_2:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarFiscal", true);
                }
                break;
            case COPIA_PRORROGA_2:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", false);
                    actionsPermitted.put("asignarFiscal", true);
                }
                break;
            case ASIGNACION_No_DESPACHO__:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case RESOLUCION_CONCEDE_PRORROGA_2:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("numeroReferencia", false);
                }
                break;
            case ASIGNACION_A_UPD__:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("asignarAUpd", true);
                }
                break;
            case ASIGNACION_DE_NUMERO_INGRESO___:
                if ((grupoCurrentUser.getId() == 2 && perfilUser.getId() == 2) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case RECIBE_SJ_PARA_MEMO:
            case REVISA_ASIGNACION_:
            case REVISA_RESOLUCION:
            case VISA_Y_FIRMA:
            case REVISA_FIRMA_Y_VISA:
            case SECRETARIA_REVISA_FIRMA_VISA:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNA_No_DESPACHO:
                if ((grupoCurrentUser.getId() == 5 && perfilUser.getId() == 8) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1))   {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case REVISA_RESOLUCION_:
            case FIRMA_RESOLUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case DESPACHA_RESOLUCION_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNA_No_RESOLUCION_DE_SOBRESEEDIMIENTO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                }
                break;
            case ASIGNA_No_INGRESO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case REDACCION_DE_MEMO_INFORMATIVO_PARA_LA_UNIDAD:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("folio", true);
                    actionsPermitted.put("reply", true);
                }
                break;
            case ELABORA_NOTIFICACION_QUE_ABSUELVE:
            case REVISA_NOTIFICACION:
            case FIRMA_NOTIFICACION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case REVISA_MEMO:
            case FIRMA_MEMO:
            case REVISA_FIRMA_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNA_No_DESPACHO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case INCULPADO_NOTIFICADO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case UNIDAD_NOTIFICADA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                }
                break;
            case ABOGADO_ELABORA_INFORME:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("siDeAcuerdo", true);
                    actionsPermitted.put("noReabro", true);
                    actionsPermitted.put("noPropone", true);
                }
                break;

            case FISCAL_NOTIFICADO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("aceptar", true);
                    actionsPermitted.put("rechazar", true);
                    actionsPermitted.put("goBackwards", true);
                } else {
                    actionsPermitted.put("watchTabRespuesta", false);
                }
                break;


            case INVESTIGACION: // requisito DONDE SE PUEDE SOLICTAR PRORROGA
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1)) {
                    actionsPermitted.put("fiscalNotificaCierre", true);
                    if (etapa != EstadoProvidencia.INVESTIGACION_PRORROGA_2){
                        actionsPermitted.put("prorroga", true);
                    }
                }
                break;

            case REDACCION_NOTIFICACION_MEMO_DEMANDANTE:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("folio", true);
                    actionsPermitted.put("notificaDemandado", true);
                }
                break;

            case SUBDIRECCION_ASIGNA_UPD_RESOLUCION_MEMO:
            case SECRETARIA_REVISA_ASIGNACION_UPD:
            case REDACCION_RESOLUCION_MEMO:
            case SECRETARIA_REVISA_FIRMA_VISA_SUBDIRECCION:
            case DGD_DESPACHA_RESOLUCION:
            case SJ_RECIBE_RESOLUCION:
            case UPD_ELABORA_NOTIFICACION_RESOLUCION:
            case SECRETARIA_REVISA_FIRMA_DE_SUBDIRECCION_A_RESOLUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;

            case NOTIFICAR_INCULPADO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("notificaDemandado", true);
                }
                break;

            case SE_NOTIFICO_INCULPADO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("apela", true);
                    actionsPermitted.put("noApela", true);
                }
                break;

            case CERTIFICACION_NO_APELA:
            case REDACCION_RESOLUCION_MEMO_NO_APELA:
            case REVISANDO_FIRMA_Y_VISA_DE_SUBDIRECCION:
            case GESTOR_DOCUMENTAL_DESPACHA_A_DN:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;

            case SECRETARIA_REVISA_RESOLUCION_MEMO:
            case ESPERANDO_FIRMA_VISA_DE_SUBDIRECCION_A_RESOLUCION:
            case ESPERANDO_FIRMA_DEL_DN_A_RESOLUCION:
            case SECRETARIA_REVISA_NOTIFICACION_RESOLUCION:
            case ESPERANDO_FIRMA_DE_SUBDIRECCION_A_RESOLUCION:
            case SECRETARIA_REVISA_INFORME_APELACION:
            case SUBDIRECCION_REVISA_INFORME_APELACION:
            case SECRETARIA_REVISA_REDACCION:
            case VISA_Y_FIRMA_SUBDIRECCION:
            case DN_FIRMA_RESOLUCION:
            case SECRETARIA_REVISA_RESOLUCION:
            case FIRMA_DEL_DN_A_RESOLUCION:
            case DN_FIRMA_RESOLUCION_NO_APELA:
            case ESPERANDO_FIRMA_Y_VISA_DE_SUBDIRECCION:
            case REVISION_RESOLUCION_MEMO_NO_APELA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;

            case TOMA_DE_RAZON_O_REGISTRA_O_REPRESENTA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("tomaRazon", true);
                    actionsPermitted.put("registra", true);
                    actionsPermitted.put("representa", true);
                }
                break;

            case TOMA_DE_RAZON:
            case REGISTRA:
            case SJ_RECIBE_PARA_REDACCION:
            case UPD_REDACTA_RESOLUCION_MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;

            case REALIZAR_NOTIFICACIONES:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("notificaDemandado", true);
                    actionsPermitted.put("notificarDGDP", true);
                    actionsPermitted.put("notificaRemuneracion", true);
                    actionsPermitted.put("notificaDenunciante", true);
                }
                break;

            case MEMO:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("notificarDGDP", true);
                    actionsPermitted.put("notificaRemuneracion", true);
                }
                break;

            case UPD_REGISTRA_INCULPADO_SI_APELA:
            case DGD_DESPACHA_APELACION_A_DN:
            case APELACION_RECIBIDA:
            case SECRETARIA_REVISA_APELACION:

            case ABOGADO_ELABORA_INFORME_APELACION:
            case SECRETARIA_DESPACHA_INFORME_APELACION_A_DGD:
            case DGD_DESPACHA_INFORME_APELACION_A_DN:
            case REVISION_DE_PROVIDENCIA_:
                if (subEtapa== EstadoProvidencia.NUEVA_PROVIDENCIA_DN){

                    if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                        actionsPermitted.put("goBackwards", true);

                    }
                    break;
                }
                break;
            case SUBDIRECCION_ASIGNA_REDACCION_RESOLUCION_MEMO:
            case SECRETARIA_REVISA_ASIGNACION_REDACCION:
            case REDACCION_RESOLUCION_MEMO_UPD:
            case SECRETARIA_REVISA_VISA_Y_FIRMA_SUBDIRECCION:
            case DESPACHA_A_DN:
            case SJ_RECIBE_RESOLUCION_CON_NUMERO:

            case SJ_RECIBIO_ALCANCE:
            case SJ_RECIBIO_RESOLUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case REDACCION_DE_RESOLUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {

                    Boolean respuestaplantilla=  this.respuestaService.findByversihayplantillaadjunta(providenciaDTO.getId());
                    log.debug("ruben2: plantila " + respuestaplantilla);

                    actionsPermitted.put("reply", respuestaplantilla);

                }
                break;
            case DN_EMITE_PROVIDENCIA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
//                        actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                    actionsPermitted.put("emiteProvidencia", true);
                }
                break;

            case SUBDIRECCION_VISA_FIRMA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("memoConductor", true);
//                        actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;

            case REALIZADO_MEMO_CONDUCTOR:
            case SECRETARIA_REVISA_VISA_FIRMA_MEMO_CONDUCTOR:
            case DESPACHO_MEMO_CONDUCTOR_A_DN:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;

            case EXAMEN_DE_LEGALIDAD:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("examenLegalidad", true);
                }
                break;

            case PROVIDENCIA_CREADA____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                    actionsPermitted.put("acogeFiscal", true);
                    actionsPermitted.put("noAcogeFiscal", true);
                }
                break;
//            case NUEVA_PROVIDENCIA_DIRECCION_NACIONAL____:
//                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
//                    actionsPermitted.put("alcance", true);
//                    actionsPermitted.put("resolucion", true);
//                }
//                break;
            case ALCANCE_O_RESOLUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("alcance", true);
                    actionsPermitted.put("resolucion", true);
                }
                break;

            case SELECCION_ALCANCE:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("alcanceConResolucion", true);
                    actionsPermitted.put("alcanceSinResolucion", true);
                }
                break;

            case NOTIFICA_E_INFORMA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("notificarDGDP", true);
                }
                break;

            case SELECCION_NOTIFICA_E_INFORMA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("suspension", true);
                    actionsPermitted.put("multa", true);
                    actionsPermitted.put("sensura", true);
                    actionsPermitted.put("destitucion", true);
                }
                break;

            case NOTIFICA_E_INFORMA_SUSPENSION:
            case NOTIFICA_E_INFORMA_MULTA:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("notificaRemuneracion", true);
                }
                break;

            case NOTIFICA_E_INFORMA_SENSURA:
            case NOTIFICA_E_INFORMA_DESTITUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("notificarDGDP", true);
                }
                break;

                //- ---- ACOGE RECURSO -----
            case FIRMA_PROVIDENCIA___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_DE_NUMERO_INGRESO_____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case REVISA_PROVIDENCIA_______:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_A_UPD___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarAUpd", true);
                }
                break;
            case DERIVA_ASIGNACION____:
            case RESOLUCION_QUE_ACOGE_INHABILIDAD:
            case REVISION_DE_RESOLUCION_Y_MEMO___:
            case FIRMA_MEMO_VISA_RESOLUCION___:
            case ENVIO_FIRMA_RESOLUCION_MEMO___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_No_DESPACHO________:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case REVISA_RESOLUCION_Y_MEMO___:
            case RESOLUCION_QUE_ACOGE_INHABILIDAD_:
            case ENVIO_A_DGDP___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_No_RESOLUCION:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                }
                break;
            case ASIGNACION_No_DE_INGRESO_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case REDACION_NOTIFICACION_FISCAL:
            case REVISION_NOTIFICACION_FISCAL__:
            case FIRMA_NOTIFICACION_FISCAL_:
            case ENVIO_A_DESPACHO___:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_No_DESPACHO_________:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case NOTIFICACION_FISCAL____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;

            // ---- NO ACOGE RECURSO----

            case FIRMA_PROVIDENCIA____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                }
                break;
            case ASIGNACION_DE_NUMERO_INGRESO______:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case REVISA_PROVIDENCIA________:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_A_UPD____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarAUpd", true);
                }
                break;
            case DERIVA_ASIGNACION_____:
            case RESOLUCION_QUE_RECHAZA_INHABILIDAD:
            case REVISION_DE_RESOLUCION_Y_MEMO____:
            case FIRMA_MEMO_VISA_RESOLUCION____:
            case ENVIO_FIRMA_RESOLUCION_MEMO____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_No_DESPACHO______:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case REVISA_RESOLUCION_Y_MEMO____:
            case RESOLUCION_QUE_RECHAZA_INHABILIDAD_:
            case ENVIO_A_DGDP____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_No_RESOLUCION_:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDGDP", true);
                }
                break;
            case ASIGNACION_No_DE_INGRESO__:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroIngreso", true);
                }
                break;
            case REDACION_NOTIFICACION_FISCAL_:
            case REVISION_NOTIFICACION_FISCAL___:
            case FIRMA_NOTIFICACION_FISCAL__:
            case ENVIO_A_DESPACHO____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("goBackwards", true);
                }
                break;
            case ASIGNACION_No_DESPACHO_______:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", true);
                    actionsPermitted.put("asignarNumeroDespacho", true);
                }
                break;
            case NOTIFICACION_FISCAL_____:
                if ((grupoCurrentUser.getId() == 1 && perfilUser.getId() == 3) || (grupoCurrentUser.getId() == 1 && perfilUser.getId() == 1) || (grupoCurrentUser.getId() == 2 && perfilUser.getId() == 5)) {
                    actionsPermitted.put("reply", false);
                }
                break;
        }
        return actionsPermitted;
    }

    /**
     * Método que permite obtener las plantillas en función del requisito en el que se encuentra la providencia.
     *
     * @param providenciaDTO
     * @return
     */
    @Transactional(readOnly = true)
    public List<PlantillaDTO> getPlantillasEnabled (ProvidenciaDTO providenciaDTO){
        List<PlantillaDTO> plantillasEnabled = null;

        switch (providenciaDTO.getRequisito()) {

//                case UPD_REDACTA_RESOLUCION_Y_MEMO:
//                case REDACCION_RESOLUCION_MEMO:
//                case ESPERANDO_FIRMA_VISA_DE_SUBDIRECCION_A_RESOLUCION:
//                    plantillasEnabled = new ArrayList<>(this.plantillaService.getAll()).stream().filter(p -> {
//                        if (p.getTipo().equals(TipoPlantilla.MEMORANDUM) || p.getTipo().equals(TipoPlantilla.RESOLUCION)) {
//                            return true;
//                        }
//                        return false;
//                    }).collect(Collectors.toList());
//                    break;
//
//                case UPD_ELABORA_NOTIFICACION_PRORROGA_1:
//                case UPD_ELABORA_NOTIFICACION_PRORROGA_2:
//                    plantillasEnabled = new ArrayList<>(this.plantillaService.getAll()).stream().filter(p -> {
//                        if (p.getTipo().equals(TipoPlantilla.RESOLUCION)) {
//                            return true;
//                        }
//                        return false;
//                    }).collect(Collectors.toList());
//                    break;
//
//                case FISCAL_REMITE_EXPEDIENTE:
//                    plantillasEnabled = new ArrayList<>(this.plantillaService.getAll()).stream().filter(p -> {
//                        if (p.getTipo().equals(TipoPlantilla.NOTIFICACION)) {
//                            return true;
//                        }
//                        return false;
//                    }).collect(Collectors.toList());
//                    break;
//
//                case INVESTIGACION:
//                    plantillasEnabled = new ArrayList<>(this.plantillaService.getAll()).stream().filter(p -> {
//                        if (p.getTipo().equals(TipoPlantilla.MEMORANDUM)) {
//                            return true;
//                        }
//                        return false;
//                    }).collect(Collectors.toList());
//
//            }
            case UPD_REDACTA_RESOLUCION_Y_MEMO:
            case REDACCION_RESOLUCION_MEMO:
            case ESPERANDO_FIRMA_VISA_DE_SUBDIRECCION_A_RESOLUCION:
            case UPD_ELABORA_NOTIFICACION_PRORROGA_1:
            case UPD_ELABORA_NOTIFICACION_PRORROGA_2:
            case FISCAL_REMITE_EXPEDIENTE:
            case INVESTIGACION_:
                // desde aqui agrega ruben
            case  REDACCION_NOTIFICACION_MEMO_DEMANDANTE:
            case REDACCION_DE_RESOLUCION:
            case REDACCION_RESOLUCION_MEMO_UPD:
            case REDACCION_RESOLUCION_MEMO_NO_APELA:
            case FISCAL_REDACTA_MEMO:
                plantillasEnabled = new ArrayList<>(this.plantillaService.getAll()).stream().filter(p -> {
                    if (p.getTipo().equals(TipoPlantilla.MEMORANDUM)
                        || p.getTipo().equals(TipoPlantilla.CERTIFICACION)
                        || p.getTipo().equals(TipoPlantilla.RESOLUCION)
                        || p.getTipo().equals(TipoPlantilla.EXPEDIENTE)
                        || p.getTipo().equals(TipoPlantilla.NOTIFICACION)
                        ) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
                break;
        }
        return plantillasEnabled;
    }

    /**
     * Método que permite obtener las providencias que consideran al mismo implicado.
     *
     * @param runImplicado
     * @param entidadImplicadaId
     * @param providenciaId
     * @return
     */
    @Transactional
    public Set<ProvidenciaDTO> findAllByRunOrEntidadImplicada (String runImplicado, Long entidadImplicadaId, Long
        providenciaId){
        if (runImplicado != null && entidadImplicadaId != 0) {
            return this.providenciaRepository.findAllByRunOrEntidadImplicada(runImplicado,
                this.entidadMapper.toEntity(this.entidadService.findOne(entidadImplicadaId).get()), providenciaId).stream()
                .map(this.providenciaMapper::toDto).collect(Collectors.toSet());
        } else if (runImplicado == null && entidadImplicadaId != 0) {
            return this.providenciaRepository.findAllByRunOrEntidadImplicada(null,
                this.entidadMapper.toEntity(this.entidadService.findOne(entidadImplicadaId).get()), providenciaId).stream()
                .map(this.providenciaMapper::toDto).collect(Collectors.toSet());
        } else if (runImplicado != null && entidadImplicadaId == 0) {
            return this.providenciaRepository.findAllByRunOrEntidadImplicada(runImplicado, null, providenciaId).stream()
                .map(this.providenciaMapper::toDto).collect(Collectors.toSet());
        }

        return new HashSet<>();
    }

    @Transactional
    public ProvidenciaDTO updateNombreFiscalAsignado (ProvidenciaDTO providenciaDTO){
        Providencia providencia = this.providenciaRepository.save(this.providenciaMapper.toEntity(providenciaDTO));
        return this.providenciaMapper.toDto(providencia);
    }

    @Transactional
    public Set<ProvidenciaDTO> findAllWithoutPagination () {
        return this.providenciaRepository.findAll().stream().map(this.providenciaMapper::toDto).collect(Collectors.toSet());
    }

    @Transactional
    public ProvidenciaDTO updateNumeroReferencia (ProvidenciaUpdateNumeroReferenciaDTO
                                                      providenciaUpdateNumeroReferenciaDTO){
        Providencia providencia = null;

        if (providenciaUpdateNumeroReferenciaDTO.getProvidenciaId() != null && providenciaUpdateNumeroReferenciaDTO
            .getProvidenciaId() > 0 && providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() != null &&
            providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia() > 0) {
            this.providenciaRepository.updateNumeroReferencia(providenciaUpdateNumeroReferenciaDTO.getNumeroReferencia(),
                providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());

            providencia = this.providenciaRepository.getOne(providenciaUpdateNumeroReferenciaDTO.getProvidenciaId());
        }

        return this.providenciaMapper.toDto(providencia);
    }

    @Transactional
    public ProvidenciaDTO updateTipoSolicitud (ProvidenciaUpdateTipoSolicitudDTO providenciaUpdateTipoSolicitudDTO){
        Providencia providencia = null;

        if (providenciaUpdateTipoSolicitudDTO.getProvidenciaId() != null && providenciaUpdateTipoSolicitudDTO
            .getProvidenciaId() > 0 && providenciaUpdateTipoSolicitudDTO.getTipoSolicitud() != null) {
            this.providenciaRepository.updateTipoSolicitud(providenciaUpdateTipoSolicitudDTO.getTipoSolicitud(),
                providenciaUpdateTipoSolicitudDTO.getProvidenciaId());

            providencia = this.providenciaRepository.getOne(providenciaUpdateTipoSolicitudDTO.getProvidenciaId());
        }
        return this.providenciaMapper.toDto(providencia);
    }

    // Metodo que obtiene la providencia madre correspondiente al tipo de Providencia a Crear
    @Transactional(readOnly = true)
    public Providencia getProvidenciaNumeroReferencia (Long numeroReferencia, String tipoProvidencia){
        log.debug("Request to get Providencia : {}", numeroReferencia);
        List<Providencia> providencias = null;

        if (tipoProvidencia == "SeleccionFiscal") {
            providencias = providenciaRepository.findByNumeroRefeSeleccionFiscal(numeroReferencia);
        } else if (tipoProvidencia == "ordenJuridico") {
            providencias = providenciaRepository.findByNumeroRefeOrdenJuridico(numeroReferencia);
        } else if (tipoProvidencia == "seleccionApelacion") {
            providencias = providenciaRepository.findByNumeroRefeSeleccionApelacion(numeroReferencia);
        }
        return providencias.get(0);
    }

    @Transactional(readOnly = true)
    public Optional<ProvidenciaDTO> findAllProrroga (Long idMadre){

        log.debug("Request to get Providencia : {}", idMadre);
        Optional result = providenciaRepository.findById(idMadre);
        return result;
    }

    @Transactional(readOnly = true)
    public Providencia getProvidenciaMadreid (Long idMadre){
        log.debug("Request to get Providencia : {}", idMadre);
        Optional<Providencia> providencia = null;

        providencia = (Optional<Providencia>) providenciaRepository.findOneWithEagerRelationships(idMadre);

        log.debug("mi providencia madre de prorroga: " + providencia.get());
        return providencia.get();
    }

    // Metodo que actualiza la providencia por tipo SEGUN SEA LA MADRE
    public ProvidenciaDTO updateProvidenciaForType (ProvidenciaUpdateForTypeDTO providenciaUpdateTypeDTO){

        Providencia providencia = null;
        log.debug("referencia del forype de la madre: " + providenciaUpdateTypeDTO.getNumeroReferencia());

        Long numeroReferencia = providenciaUpdateTypeDTO.getNumeroReferencia();
        Providencia providenciaMadre = providenciaRepository.findOne(providenciaUpdateTypeDTO.getProvidenciaMadreId());
        EstadoProvidencia requisitoMadre = providenciaMadre.getRequisito();
        log.debug("El requisito de la madre para actualizar la provi es " + requisitoMadre);

        EstadoProvidencia etapaActualizada = null;

        switch (requisitoMadre) {

            case FISCAL_RECHAZO:
                etapaActualizada = EstadoProvidencia.PROVIDENCIA_SELECCION_FISCAL; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa3 = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL___; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.PROVIDENCIA_CREADA____);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa3);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            // Aqui se relaciona una Providencia Seleccion Fiscal
//            case FISCAL_RECHAZO:
//                etapaActualizada = EstadoProvidencia.PROVIDENCIA_SELECCION_FISCAL;
//
//                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());
//                log.debug("la provi Hija es " + providencia);
//
//                if (providencia.getProvidencia_madre_id() == null) {
//                    log.debug("el id de la Provimadre a setear PROVIfISCAL es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
//                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
//                }
//
//                providencia.setNumeroReferencia(numeroReferencia);
//                providencia.setEtapa(etapaActualizada);
//                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
//                providenciaMadre.setStandby(true);
//                break;

            // Aqui se relaciona una Providencia Prorroga 1 y Prorroga 2
            case PETICION_PRORROGA_1:
            case PETICION_PRORROGA_2:
                log.debug("entro en las prorrogas");

                if (requisitoMadre == EstadoProvidencia.PETICION_PRORROGA_2) {
                    etapaActualizada = EstadoProvidencia.PROVIDENCIA_PRORROGA_2;
                } else {
                    etapaActualizada = EstadoProvidencia.PROVIDENCIA_PRORROGA_1;
                }

                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear  PRORROGA es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }

                providencia.setNombreFiscalAsignado(providenciaMadre.getNombreFiscalAsignado());
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            // Aqui se relaciona una Providencia Prorroga 1 y Prorroga 2
//            case PROVIDENCIA_CREADA_:
            case REVISION_DE_PROVIDENCIA_:
                log.debug("entro en las prorrogas");

                if (requisitoMadre == EstadoProvidencia.REVISION_DE_PROVIDENCIA_) {
                    etapaActualizada = EstadoProvidencia.PROVIDENCIA_PRORROGA_2;
                } else {
                    etapaActualizada = EstadoProvidencia.PROVIDENCIA_PRORROGA_1;
                }

                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear  PRORROGA es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }

                providencia.setNombreFiscalAsignado(providenciaMadre.getNombreFiscalAsignado());
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            // Aqui se relaciona una Providencia Orden Juridico (Sancion, Sobreceder, Absolver, Reabrir)
//            case DGD_DESPACHA_SUMARIO_COMPLETO:
            case No_DESPACHO_SJ:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO;
                EstadoProvidencia subEtapaActualizada = determinarSubEtapa(providenciaUpdateTypeDTO.getOrdenJuridico());
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                if (subEtapaActualizada.toString() == "ABSOLVER"){
                    providencia.setRequisito(EstadoProvidencia.NUEVA_PROVIDENCIA_ORDENANDO_SOBRESEDIMIENTO_);
                }else  if (subEtapaActualizada.toString() == "SANCIONA"){
                    providencia.setRequisito(EstadoProvidencia.PROVIDENCIA_APLICA_SANCION);
                }
                else{
                    providencia.setRequisito(EstadoProvidencia.NUEVA_PROVIDENCIA_ORDENANDO_SOBRESEDIMIENTO);
                }
//                providencia.setRequisito(EstadoProvidencia.NUEVA_PROVIDENCIA_ORDENANDO_SOBRESEDIMIENTO);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapaActualizada);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case APELA:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa4 = EstadoProvidencia.SANCIONAR_SI_APELA; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.ASESOR_REVISA_PROVIDENCIA_APELACION);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa4);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case NO_APELA:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa6 = EstadoProvidencia.SANCIONAR_NO_APELA; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.REALIZA_CERTIFICACION);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa6);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case DESPACHO:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa5 = EstadoProvidencia.PRONUNCIANDO_RECURSO_; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.REVISA_PRONUNCIAMIENTO);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa5);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case PROVIDENCIA_CREADA_:
            case INVESTIGACION:

                etapaActualizada = EstadoProvidencia.INVESTIGACION_PRORROGA_1; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa13 = EstadoProvidencia.NUEVA_PROVIDENCIA_DN; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.REVISA_PROVIDENCIA_);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa13);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case RECEPCION_CONTRALORIA_TOMA_DE_RAZON_:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa7 = EstadoProvidencia.TOMA_DE_RAZON__; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.RECIBE_DGDP);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa7);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case TOMA_DE_RAZON____:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa8 = EstadoProvidencia.TOMA_DE_RAZON_NO_APELA; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.RECIBE_DGDP__);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa8);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case REGISTRA__:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa9 = EstadoProvidencia.REGISTRA_NO_APELA; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.RECIBE_DGDP___);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa9);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case REPRESENTA__:

                etapaActualizada = EstadoProvidencia.INFORME_JURIDICO; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa10 = EstadoProvidencia.REPRESENTA_NO_APELA; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.RECIBE_DGDP____);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa10);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case SOLICITUD_PRORROGA_2:

                etapaActualizada = EstadoProvidencia.INVESTIGACION_PRORROGA_2; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa11 = EstadoProvidencia.NUEVA_PROVIDENCIA_DIRECCION_NACIONAL; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.REVISION_DE_PROVIDENCIA___);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa11);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case ALCANCE_SIN_RESOLUCION:

                etapaActualizada = EstadoProvidencia.PROVIDENCIA_SIN_RESOLUCION; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa = EstadoProvidencia.SANCIONA_SIN_RESOLUCION; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;

            case ENVIA_VISTA_FISCAL_A_DN__:
            case ENVIA_VISTA_FISCAL_A_DN___:
            case REMITE_EXPEDIENTE_VISTA_FISCAL:
            case ENVIA_VISTA_FISCAL_A_DN_:



                etapaActualizada = EstadoProvidencia.INVESTIGACION; // ETAPA DE PRUEBA COLOCAR EL INDICADO
                EstadoProvidencia subEtapa2 = EstadoProvidencia.VISTA_FISCAL_DIRECCION_NACIONAL; // SUBETAPA DE PRUEBA COLOCAR EL INDICADO
                providencia = this.providenciaRepository.getOne(providenciaUpdateTypeDTO.getProvidenciaId());

                if (providencia.getProvidencia_madre_id() == null) {
                    log.debug("el id de la Provimadre a setear ORDEN JURIDICO es " + providenciaUpdateTypeDTO.getProvidenciaMadreId());
                    providencia.setProvidencia_madre_id(providenciaMadre.getId());
                }
                providencia.setRequisito(EstadoProvidencia.ASIGNACION_A_SJ);
                providencia.setNumeroReferencia(numeroReferencia);
                providencia.setEtapa(etapaActualizada);
                providencia.setSubEtapa(subEtapa2);
                providencia.setEstadoActual(this.concatenarEstado(providencia.getRequisito(), providencia.getSubEtapa(), etapaActualizada));
                providenciaMadre.setStandby(true);
                break;
        }
        return this.providenciaMapper.toDto(providencia);
    }

    private EstadoProvidencia determinarSubEtapa(OrdenJuridico ordenJuridico) {

        EstadoProvidencia subEtapaNueva = null;

        switch (ordenJuridico) {
            case SOBRECEDER:
                subEtapaNueva = EstadoProvidencia.SOBRESEER;
                break;
            case ABSOLVER:
                subEtapaNueva = EstadoProvidencia.ABSOLVER;
                break;
            case SANCIONAR:
                subEtapaNueva = EstadoProvidencia.SANCIONA;
                break;
            case REABRIR:
                subEtapaNueva = EstadoProvidencia.REABRIR;
                break;
        }
        return subEtapaNueva;
    }

}

