import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { PdisciplinarioSharedModule } from 'app/shared';
import {
    ProvidenciaComponent,
    ProvidenciaDetailComponent,
    ProvidenciaDeletePopupComponent,
    ProvidenciaDeleteDialogComponent,
    providenciaRoute,
    providenciaPopupRoute
} from './';

import { FormsModule } from '@angular/forms';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { MovimientoProvidenciaComponent } from 'app/entities/movimiento-providencia';
import { ProvidenciaResponderDialogComponent, ProvidenciaResponderPopupComponent } from 'app/entities/providencia/providencia-responder-dialog.component';
import { ProvidenciaDevolverDialogComponent, ProvidenciaDevolverPopupComponent } from 'app/entities/providencia/providencia-devolver-dialog.component';
import { DerivacionComponent } from '../derivacion/derivacion.component';
import { AdjuntoShowListPopupComponent } from '../adjunto/adjunto-show-list-dialog.component';
import { CKEditorModule } from 'ng2-ckeditor';
import { ProvidenciaDetailDgdpComponent } from 'app/entities/providencia/providencia-detail-dgdp.component';
import {
    ProvidenciaAsignarNumeroResolucionComponent,
    ProvidenciaAsignarNumeroResolucionPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-resolucion.component';
import { RespuestaUpdateComponent } from '../respuesta/respuesta-update.component';
import { ComentarioBitacoraModalComponent } from 'app/entities/movimiento-providencia/comentario-bitacora-modal.component';
import { RespuestaBitacoraModalComponent } from 'app/entities/respuesta/respuesta-bitacora-modal.componente';
import { ProvidenciaRelacionadasComponent } from './providencia-relacionadas.component';

import { Ng2Rut } from 'ng2-rut';

import {
    ProvidenciaRelacionarDialogComponent,
    ProvidenciaRelacionarPopupComponent
} from './providencia-relacionar-dialog.component';

import {
    ProvidenciaTerminoProbatorioComponent,
    ProvidenciaTerminoProbatorioPopupComponent
} from './providencia-termino-probatorio.component';

import {
    ProvidenciaSiDeAcuerdoComponent,
    ProvidenciaSiDeAcuerdoPopupComponent
} from './providencia-si-de-acuerdo.component';

import {
    ProvidenciaAsignarFiscalComponent,
    ProvidenciaAsignarFiscalPopupComponent
} from './providencia-asignar-fiscal.component';
import {
    ProvidenciaAsignarNumeroReferenciaComponent,
    ProvidenciaAsignarNumeroReferenciaPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-referencia.component';
import {
    ProvidenciaAsignarTipoSolicitudComponent,
    ProvidenciaAsignarTipoSolicitudPopupComponent
} from 'app/entities/providencia/providencia-asignar-tipo-solicitud.component';
import {
    ProvidenciaFiscalAceptaComponent,
    ProvidenciaFiscalAceptaPopupComponent
} from 'app/entities/providencia/providencia-fiscal-acepta.component';
import {
    ProvidenciaFiscalRechazaComponent,
    ProvidenciaFiscalRechazaPopupComponent
} from 'app/entities/providencia/providencia-fiscal-rechaza.component';
import {
    ProvidenciaFiscalProrrogaComponent,
    ProvidenciaFiscalProrrogaPopupComponent
} from 'app/entities/providencia/providencia-fiscal-prorroga.component';
import {
    ProvidenciaApelaComponent,
    ProvidenciaApelaPopupComponent
} from 'app/entities/providencia/providencia-apela.component';
import {
    ProvidenciaNoProponeComponent,
    ProvidenciaNoProponePopupComponent
} from 'app/entities/providencia/providencia-no-propone.component';

import {

    ProvidenciaNoReabroComponent,
    ProvidenciaNoReabroPopupComponent
} from 'app/entities/providencia/providencia-no-reabro.component';

import {
    ProvidenciaNoApelaComponent,
    ProvidenciaNoApelaPopupComponent
} from 'app/entities/providencia/providencia-no-apela.component';
import {
    ProvidenciaFiscalNotificaCierreComponent,
    ProvidenciaFiscalNotificaCierrePopupComponent
} from 'app/entities/providencia/providencia-fiscal-notifica-cierre-investigacion.component';
import {
    ProvidenciaInculpadoNoEnviaMemoComponent,
    ProvidenciaInculpadoNoEnviaMemoPopupComponent
} from 'app/entities/providencia/providencia-inculpado-no-envia-memo.component';
import {
    ProvidenciaInculpadoEnviaMemoComponent,
    ProvidenciaInculpadoEnviaMemoPopupComponent
} from 'app/entities/providencia/providencia-inculpado-envia-memo.component';
import {
    ProvidenciaFiscalFormulaCargosComponent,
    ProvidenciaFiscalFormulaCargosPopupComponent
} from 'app/entities/providencia/providencia-fiscal-formula-cargos.component';
import {
    ProvidenciaFiscalRemiteExpedienteComponent,
    ProvidenciaFiscalRemiteExpedientePopupComponent
} from 'app/entities/providencia/providencia-fiscal-remite-expediente.component';
import {
    ProvidenciaAsignarNumeroFolioComponent,
    ProvidenciaAsignarNumeroFolioPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-folio.component';

import {
    ProvidenciaUpdNotificaInculpadoComponent,
    ProvidenciaUpdNotificaInculpadoPopupComponent
} from 'app/entities/providencia/providencia-upd-notifica-inculpado.component';
import {
    ProvidenciaRepresentaComponent,
    ProvidenciaRepresentaPopupComponent
} from 'app/entities/providencia/providencia-representa.component';
import {
    ProvidenciaTomaRazonComponent,
    ProvidenciaTomaRazonPopupComponent
} from 'app/entities/providencia/providencia-toma-razon.component';
import {
    ProvidenciaRegistraComponent,
    ProvidenciaRegistraPopupComponent
} from 'app/entities/providencia/providencia-registra.component';
import {
    ProvidenciaMemoConductorComponent,
    ProvidenciaMemoConductorPopupComponent
} from 'app/entities/providencia/providencia-memo-conductor.component';
import {
    ProvidenciaExamenLegalidadComponent,
    ProvidenciaExamenLegalidadPopupComponent
} from 'app/entities/providencia/providencia-examen-legalidad.component';
import {
    ProvidenciaAlcanceComponent,
    ProvidenciaAlcancePopupComponent
} from 'app/entities/providencia/providencia-alcance.component';
import {
    ProvidenciaResolucionComponent,
    ProvidenciaResolucionPopupComponent
} from 'app/entities/providencia/providencia-resolucion.component';
import {
    ProvidenciaSinResolucionComponent,
    ProvidenciaSinResolucionPopupComponent
} from 'app/entities/providencia/providencia-sin-resolucion.component';
import {ProvidenciaRealizoResolucionComponent,
    ProvidenciaRealizoResolucionPopupComponent} from 'app/entities/providencia/providencia-realizo-resolucion.component';
import {
    ProvidenciaDgdpNotificaInculpadoComponent,
    ProvidenciaDgdpNotificaInculpadoPopupComponent
} from 'app/entities/providencia/providencia-dgdp-notifica-inculpado.component';
import {
    ProvidenciaSuspensionComponent,
    ProvidenciaSuspensionPopupComponent
} from 'app/entities/providencia/providencia-suspension.component';
import {
    ProvidenciaMultaComponent,
    ProvidenciaMultaPopupComponent
} from 'app/entities/providencia/providencia-multa.component';
import {
    ProvidenciaSensuraComponent,
    ProvidenciaSensuraPopupComponent
} from 'app/entities/providencia/providencia-sensura.component';

import {  ProvidenciaDestitucionComponent,
    ProvidenciaDestitucionPopupComponent
} from 'app/entities/providencia/providencia-destitucion.component';

import { ProvidenciaNotificaRemuneracionComponent,
    ProvidenciaNotificaRemuneracionPopupComponent
} from 'app/entities/providencia/providencia-notifica-remuneracion.component';
import {
    ProvidenciaNotificaDenuncianteComponent,
    ProvidenciaNotificaDenunciantePopupComponent
} from 'app/entities/providencia/providencia-notifica-denunciante.component';
import {
    ProvidenciaAsignarNumeroDgdComponent,
    ProvidenciaAsignarNumeroDgdPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-dgd.component';
import {
    ProvidenciaAsignarNumeroProvidenciaComponent,
    ProvidenciaAsignarNumeroProvidenciaPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-providencia.component';
import {
    ProvidenciaEmiteComponent,
    ProvidenciaEmitePopupComponent
} from 'app/entities/providencia/providencia-emite.component';
import {
    ProvidenciaAsignarNumeroDgdpComponent,
    ProvidenciaAsignarNumeroDgdpPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-dgdp.component';
import {
    ProvidenciaAsignarAbogadoComponent,
    ProvidenciaAsignarAbogadoPopupComponent
} from 'app/entities/providencia/providencia-asignar-abogado.component';
import {
    ProvidenciaAsignarNumeroIngresoComponent,
    ProvidenciaAsignarNumeroIngresoPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-ingreso.component';
import {
    ProvidenciaAsignarAUpdComponent,
    ProvidenciaAsignarAUpdPopupComponent
} from 'app/entities/providencia/providencia-asignar-a-upd.component';
import {
    ProvidenciaSinDenuncianteComponent,
    ProvidenciaSinDenunciantePopupComponent
} from 'app/entities/providencia/providencia-sin-denunciante.component';
import {
    ProvidenciaAsignarNumeroDespachoComponent,
    ProvidenciaAsignarNumeroDespachoPopupComponent
} from 'app/entities/providencia/providencia-asignar-numero-despacho.component';
import {
    ProvidenciaEnviarInformeComponent,
    ProvidenciaEnviarInformePopupComponent
} from 'app/entities/providencia/providencia-enviar-informe.component';
import {
    ProvidenciaEnviaVistaFiscalComponent,
    ProvidenciaEnviaVistaFiscalPopupComponent
} from 'app/entities/providencia/providencia-envia-vista-fiscal.component';
import {
    ProvidenciaNotificaResolucionComponent,
    ProvidenciaNotificaResolucionPopupComponent
} from 'app/entities/providencia/providencia-notifica-resolucion.component';
import {
    ProvidenciaAcogeComponent,
    ProvidenciaAcogePopupComponent
} from 'app/entities/providencia/providencia-acoge.component';
import {
    ProvidenciaAcogeParcialComponent,
    ProvidenciaAcogeParcialPopupComponent
} from 'app/entities/providencia/providencia-acoge-parcial.component';
import {
    ProvidenciaRechazaComponent,
    ProvidenciaRechazaPopupComponent
} from 'app/entities/providencia/providencia-rechaza.component';
import {
    ProvidenciaNotificaDenunciadoComponent,
    ProvidenciaNotificaDenunciadoPopupComponent
} from 'app/entities/providencia/providencia-notifica-denunciado.component';
import {
    ProvidenciaNotificaDgdpComponent,
    ProvidenciaNotificaDgdpPopupComponent
} from 'app/entities/providencia/providencia-notifica-dgdp.component';

//sadasda
import {
    ProvidenciaAcogeFiscalPopupComponent,
    ProvidenciaAcogeFiscalComponent}
    from 'app/entities/providencia/providencia-acoge-fiscal.component';
import {
    ProvidenciaNoAcogeFiscalPopupComponent,
    ProvidenciaNoAcogeFiscalComponent
} from './providencia-no-acoge-fiscal.component';
import {
    ProvidenciaCierreInvestigacionSumariaPopupComponent
    ,ProvidenciaCierreInvestigacionSumariaComponent
} from './providencia-cierre-investigacion-sumaria.component';
import {
    ProvidenciaElevarsumarioAdministrativoPopupComponent,
    ProvidenciaElevarsumarioAdministrativoComponent
} from './providencia-elevar-sumario-administrativo.component';
import {ProvidenciaApelaInvestigacionPopupComponent
    , ProvidenciaApelaInvestigacionComponent
} from './providencia-apela-investigacion.component';
import {
    ProvidenciaAcogeInvestigacionApelaPopupComponent,
    ProvidenciaAcogeInvestigacionApelaComponent} from './providencia-acoge-investigacion-apela.component';
import {
    ProvidenciaAceptaInvestigadorNoAceptaPopupComponent,
    ProvidenciaAceptaInvestigadorNoAceptaComponent
} from './providencia-acepta-investigador-no-acepta.component';
// directive
import {
    ReadMoreDirective,
} from 'app/entities/providencia/read-more.directive';
import { ProvidenciaUpdatePopupComponent, ProvidenciaUpdateComponent } from 'app/entities/providencia/providencia-update.component';

const ENTITY_STATES = [...providenciaRoute, ...providenciaPopupRoute];

@NgModule({
    imports: [
        Ng2Rut,
        PdisciplinarioSharedModule,
        RouterModule.forChild(ENTITY_STATES),
        // ,{onSameUrlNavigation: 'reload'}
        AngularMultiSelectModule,
        FormsModule,
        CKEditorModule
    ],
    declarations: [
        ProvidenciaComponent,
        ProvidenciaDetailComponent,
        ProvidenciaDetailDgdpComponent,
        ProvidenciaUpdatePopupComponent,
        ProvidenciaUpdateComponent,
        ProvidenciaDeleteDialogComponent,
        ProvidenciaResponderDialogComponent,
        ProvidenciaResponderPopupComponent,
        ProvidenciaFiscalAceptaComponent,
        ProvidenciaFiscalAceptaPopupComponent,
        ProvidenciaDeletePopupComponent,
        ProvidenciaDevolverDialogComponent,
        ProvidenciaDevolverPopupComponent,
        MovimientoProvidenciaComponent,
        DerivacionComponent,
        AdjuntoShowListPopupComponent,
        RespuestaBitacoraModalComponent,
        ComentarioBitacoraModalComponent,
        ProvidenciaAsignarNumeroResolucionComponent,
        ProvidenciaAsignarNumeroResolucionPopupComponent,
        ProvidenciaAsignarNumeroReferenciaComponent,
        ProvidenciaAsignarNumeroReferenciaPopupComponent,
        RespuestaUpdateComponent,
        ProvidenciaRelacionadasComponent,
        ProvidenciaRelacionarPopupComponent,
        ProvidenciaRelacionarDialogComponent,
        ProvidenciaAsignarFiscalComponent,
        ProvidenciaAsignarFiscalPopupComponent,
        ProvidenciaAsignarTipoSolicitudComponent,
        ProvidenciaAsignarTipoSolicitudPopupComponent,
        ProvidenciaFiscalRechazaComponent,
        ProvidenciaFiscalRechazaPopupComponent,
        ProvidenciaFiscalProrrogaComponent,
        ProvidenciaFiscalProrrogaPopupComponent,
        ProvidenciaApelaPopupComponent,
        ProvidenciaApelaComponent,
        ProvidenciaNoApelaPopupComponent,
        ProvidenciaNoApelaComponent,
        ProvidenciaFiscalNotificaCierreComponent,
        ProvidenciaFiscalNotificaCierrePopupComponent,
        ProvidenciaInculpadoNoEnviaMemoComponent,
        ProvidenciaInculpadoNoEnviaMemoPopupComponent,
        ProvidenciaInculpadoEnviaMemoComponent,
        ProvidenciaInculpadoEnviaMemoPopupComponent,
        ProvidenciaFiscalRemiteExpedienteComponent,
        ProvidenciaFiscalRemiteExpedientePopupComponent,
        ProvidenciaFiscalFormulaCargosComponent,
        ProvidenciaFiscalFormulaCargosPopupComponent,
        ProvidenciaTerminoProbatorioComponent,
        ProvidenciaTerminoProbatorioPopupComponent,
        ProvidenciaSiDeAcuerdoComponent,
        ProvidenciaSiDeAcuerdoPopupComponent,
        ProvidenciaNoProponeComponent,
        ProvidenciaNoProponePopupComponent,
        ProvidenciaNoReabroComponent,
        ProvidenciaNoReabroPopupComponent,
        ReadMoreDirective,
        ProvidenciaAsignarNumeroFolioComponent,
        ProvidenciaAsignarNumeroFolioPopupComponent,
        ProvidenciaUpdNotificaInculpadoComponent,
        ProvidenciaUpdNotificaInculpadoPopupComponent,
        ProvidenciaTomaRazonComponent,
        ProvidenciaTomaRazonPopupComponent,
        ProvidenciaRepresentaComponent,
        ProvidenciaRepresentaPopupComponent,
        ProvidenciaRegistraComponent,
        ProvidenciaRegistraPopupComponent,
        ProvidenciaMemoConductorComponent,
        ProvidenciaMemoConductorPopupComponent,
        ProvidenciaExamenLegalidadComponent,
        ProvidenciaExamenLegalidadPopupComponent,
        ProvidenciaAlcanceComponent,
        ProvidenciaAlcancePopupComponent,
        ProvidenciaResolucionComponent,
        ProvidenciaResolucionPopupComponent,
        ProvidenciaSinResolucionComponent,
        ProvidenciaSinResolucionPopupComponent,
        ProvidenciaRealizoResolucionComponent,
        ProvidenciaRealizoResolucionPopupComponent,
        ProvidenciaDgdpNotificaInculpadoComponent,
        ProvidenciaDgdpNotificaInculpadoPopupComponent,
        ProvidenciaSuspensionComponent,
        ProvidenciaSuspensionPopupComponent,
        ProvidenciaMultaComponent,
        ProvidenciaMultaPopupComponent,
        ProvidenciaSensuraComponent,
        ProvidenciaSensuraPopupComponent,
        ProvidenciaDestitucionComponent,
        ProvidenciaDestitucionPopupComponent,
        ProvidenciaNotificaRemuneracionComponent,
        ProvidenciaNotificaRemuneracionPopupComponent,
        ProvidenciaNotificaDenuncianteComponent,
        ProvidenciaNotificaDenunciantePopupComponent,

        ProvidenciaAsignarNumeroDgdComponent,
        ProvidenciaAsignarNumeroDgdPopupComponent,
        ProvidenciaAsignarNumeroProvidenciaComponent,
        ProvidenciaAsignarNumeroProvidenciaPopupComponent,
        ProvidenciaEmiteComponent,
        ProvidenciaEmitePopupComponent,
        ProvidenciaAsignarNumeroDgdpComponent,
        ProvidenciaAsignarNumeroDgdpPopupComponent,
        ProvidenciaAsignarAbogadoComponent,
        ProvidenciaAsignarAbogadoPopupComponent,
        ProvidenciaAsignarNumeroIngresoComponent,
        ProvidenciaAsignarNumeroIngresoPopupComponent,
        ProvidenciaAsignarAUpdComponent,
        ProvidenciaAsignarAUpdPopupComponent,
        ProvidenciaSinDenuncianteComponent,
        ProvidenciaSinDenunciantePopupComponent,
        ProvidenciaAsignarNumeroDespachoComponent,
        ProvidenciaAsignarNumeroDespachoPopupComponent,
        ProvidenciaEnviarInformeComponent,
        ProvidenciaEnviarInformePopupComponent,
        ProvidenciaEnviaVistaFiscalComponent,
        ProvidenciaEnviaVistaFiscalPopupComponent,
        ProvidenciaNotificaResolucionComponent,
        ProvidenciaNotificaResolucionPopupComponent,
        ProvidenciaAcogeComponent,
        ProvidenciaAcogePopupComponent,
        ProvidenciaAcogeParcialComponent,
        ProvidenciaAcogeParcialPopupComponent,
        ProvidenciaRechazaComponent,
        ProvidenciaRechazaPopupComponent,
        ProvidenciaNotificaDenunciadoComponent,
        ProvidenciaNotificaDenunciadoPopupComponent,
        ProvidenciaNotificaDgdpComponent,
        ProvidenciaNotificaDgdpPopupComponent,

        ProvidenciaAcogeFiscalPopupComponent,
        ProvidenciaAcogeFiscalComponent,
        ProvidenciaNoAcogeFiscalPopupComponent,
        ProvidenciaNoAcogeFiscalComponent,
        //test
        ProvidenciaAceptaInvestigadorNoAceptaPopupComponent,
        ProvidenciaAceptaInvestigadorNoAceptaComponent,
        ProvidenciaApelaInvestigacionPopupComponent,
        ProvidenciaApelaInvestigacionComponent,
        ProvidenciaAcogeInvestigacionApelaPopupComponent,
        ProvidenciaAcogeInvestigacionApelaComponent,
        ProvidenciaElevarsumarioAdministrativoPopupComponent,
        ProvidenciaElevarsumarioAdministrativoComponent,
        ProvidenciaCierreInvestigacionSumariaPopupComponent,
        ProvidenciaCierreInvestigacionSumariaComponent,



    ],
    entryComponents: [
        ProvidenciaComponent,
        ProvidenciaUpdatePopupComponent,
        ProvidenciaUpdateComponent,
        ProvidenciaDeleteDialogComponent,
        ProvidenciaResponderDialogComponent,
        ProvidenciaResponderPopupComponent,
        ProvidenciaFiscalAceptaComponent,
        ProvidenciaFiscalAceptaPopupComponent,
        ProvidenciaDeletePopupComponent,
        ProvidenciaDevolverDialogComponent,
        ProvidenciaDevolverPopupComponent,
        AdjuntoShowListPopupComponent,
        RespuestaBitacoraModalComponent,
        ComentarioBitacoraModalComponent,
        ProvidenciaAsignarNumeroResolucionComponent,
        ProvidenciaAsignarNumeroResolucionPopupComponent,
        ProvidenciaAsignarNumeroReferenciaComponent,
        ProvidenciaAsignarNumeroReferenciaPopupComponent,
        RespuestaUpdateComponent,
        ProvidenciaRelacionadasComponent,
        ProvidenciaRelacionarPopupComponent,
        ProvidenciaRelacionarDialogComponent,
        ProvidenciaAsignarFiscalComponent,
        ProvidenciaAsignarFiscalPopupComponent,
        ProvidenciaAsignarTipoSolicitudComponent,
        ProvidenciaAsignarTipoSolicitudPopupComponent,
        ProvidenciaFiscalRechazaComponent,
        ProvidenciaFiscalRechazaPopupComponent,
        ProvidenciaFiscalProrrogaComponent,
        ProvidenciaFiscalProrrogaPopupComponent,
        ProvidenciaApelaPopupComponent,
        ProvidenciaApelaComponent,
        ProvidenciaNoApelaPopupComponent,
        ProvidenciaNoApelaComponent,
        ProvidenciaFiscalNotificaCierreComponent,
        ProvidenciaFiscalNotificaCierrePopupComponent,
        ProvidenciaInculpadoNoEnviaMemoComponent,
        ProvidenciaInculpadoNoEnviaMemoPopupComponent,
        ProvidenciaInculpadoEnviaMemoComponent,
        ProvidenciaInculpadoEnviaMemoPopupComponent,
        ProvidenciaFiscalRemiteExpedienteComponent,
        ProvidenciaFiscalRemiteExpedientePopupComponent,
        ProvidenciaFiscalFormulaCargosComponent,
        ProvidenciaFiscalFormulaCargosPopupComponent,
        ProvidenciaTerminoProbatorioComponent,
        ProvidenciaTerminoProbatorioPopupComponent,
        ProvidenciaSiDeAcuerdoComponent,
        ProvidenciaSiDeAcuerdoPopupComponent,
        ProvidenciaNoProponeComponent,
        ProvidenciaNoProponePopupComponent,
        ProvidenciaNoReabroComponent,
        ProvidenciaNoReabroPopupComponent,
        ProvidenciaAsignarNumeroFolioComponent,
        ProvidenciaAsignarNumeroFolioPopupComponent,
        ProvidenciaUpdNotificaInculpadoComponent,
        ProvidenciaUpdNotificaInculpadoPopupComponent,
        ProvidenciaTomaRazonComponent,
        ProvidenciaTomaRazonPopupComponent,
        ProvidenciaRepresentaComponent,
        ProvidenciaRepresentaPopupComponent,
        ProvidenciaRegistraComponent,
        ProvidenciaRegistraPopupComponent,
        ProvidenciaMemoConductorComponent,
        ProvidenciaMemoConductorPopupComponent,
        ProvidenciaExamenLegalidadComponent,
        ProvidenciaExamenLegalidadPopupComponent,
        ProvidenciaAlcanceComponent,
        ProvidenciaAlcancePopupComponent,
        ProvidenciaResolucionComponent,
        ProvidenciaResolucionPopupComponent,
        ProvidenciaSinResolucionComponent,
        ProvidenciaSinResolucionPopupComponent,
        ProvidenciaRealizoResolucionComponent,
        ProvidenciaRealizoResolucionPopupComponent,
        ProvidenciaDgdpNotificaInculpadoComponent,
        ProvidenciaDgdpNotificaInculpadoPopupComponent,
        ProvidenciaSuspensionComponent,
        ProvidenciaSuspensionPopupComponent,
        ProvidenciaMultaComponent,
        ProvidenciaMultaPopupComponent,
        ProvidenciaSensuraComponent,
        ProvidenciaSensuraPopupComponent,
        ProvidenciaDestitucionComponent,
        ProvidenciaDestitucionPopupComponent,
        ProvidenciaNotificaRemuneracionComponent,
        ProvidenciaNotificaRemuneracionPopupComponent,
        ProvidenciaNotificaDenuncianteComponent,
        ProvidenciaNotificaDenunciantePopupComponent,
        ProvidenciaAsignarNumeroDgdComponent,
        ProvidenciaAsignarNumeroDgdPopupComponent,
        ProvidenciaAsignarNumeroProvidenciaComponent,
        ProvidenciaAsignarNumeroProvidenciaPopupComponent,
        ProvidenciaEmiteComponent,
        ProvidenciaEmitePopupComponent,
        ProvidenciaAsignarNumeroDgdpComponent,
        ProvidenciaAsignarNumeroDgdpPopupComponent,
        ProvidenciaAsignarAbogadoComponent,
        ProvidenciaAsignarAbogadoPopupComponent,
        ProvidenciaAsignarNumeroIngresoComponent,
        ProvidenciaAsignarNumeroIngresoPopupComponent,
        ProvidenciaAsignarAUpdComponent,
        ProvidenciaAsignarAUpdPopupComponent,
        ProvidenciaSinDenuncianteComponent,
        ProvidenciaSinDenunciantePopupComponent,
        ProvidenciaAsignarNumeroDespachoComponent,
        ProvidenciaAsignarNumeroDespachoPopupComponent,
        ProvidenciaEnviarInformeComponent,
        ProvidenciaEnviarInformePopupComponent,
        ProvidenciaEnviaVistaFiscalComponent,
        ProvidenciaEnviaVistaFiscalPopupComponent,
        ProvidenciaNotificaResolucionComponent,
        ProvidenciaNotificaResolucionPopupComponent,
        ProvidenciaAcogeComponent,
        ProvidenciaAcogePopupComponent,
        ProvidenciaAcogeParcialComponent,
        ProvidenciaAcogeParcialPopupComponent,
        ProvidenciaRechazaComponent,
        ProvidenciaRechazaPopupComponent,
        ProvidenciaNotificaDenunciadoComponent,
        ProvidenciaNotificaDenunciadoPopupComponent,
        ProvidenciaNotificaDgdpComponent,
        ProvidenciaNotificaDgdpPopupComponent,

        ProvidenciaAcogeFiscalPopupComponent,
        ProvidenciaAcogeFiscalComponent,
        ProvidenciaNoAcogeFiscalPopupComponent,
        ProvidenciaNoAcogeFiscalComponent,

        ProvidenciaAceptaInvestigadorNoAceptaPopupComponent,
        ProvidenciaAceptaInvestigadorNoAceptaComponent,
        ProvidenciaApelaInvestigacionPopupComponent,
        ProvidenciaApelaInvestigacionComponent,
        ProvidenciaAcogeInvestigacionApelaPopupComponent,
        ProvidenciaAcogeInvestigacionApelaComponent,
        ProvidenciaElevarsumarioAdministrativoPopupComponent,
        ProvidenciaElevarsumarioAdministrativoComponent,
        ProvidenciaCierreInvestigacionSumariaPopupComponent,
        ProvidenciaCierreInvestigacionSumariaComponent,

    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PdisciplinarioProvidenciaModule {}
