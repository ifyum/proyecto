import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import {DATE_FORMAT, DATE_TIME_FORMAT} from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';

import { InstruccionesProvidencia, IProvidencia } from 'app/shared/model/providencia.model';
import { ProvidenciaService } from './providencia.service';
import { IGrupo } from 'app/shared/model/grupo.model';
import {IEntidad} from '../../shared/model/entidad.model';
import {EntidadService} from '../entidad/entidad.service';
import {NgbActiveModal, NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {ProvidenciaResponderDialogComponent} from "app/entities/providencia/providencia-responder-dialog.component";

@Component({
    selector: 'jhi-providencia-update',
    templateUrl: './providencia-update.component.html',
    styleUrls: ['./providencia-update.css']
})
export class ProvidenciaUpdateComponent implements OnInit {
    // Variables para el multiCheck
    dropdownListAcciones = [];
    selectedAcciones = [];
    dropdownSettingsAcciones = {};
    // Atributos de la clase
    private _providencia: IProvidencia;
    isSaving: boolean;
    cargarTipoSolicitud: IProvidencia;
    fechaSolicitud: string;
    fechaCreacion: string;
    entidades: IEntidad[];

    constructor(
        private jhiAlertService: JhiAlertService,
        public activeModal: NgbActiveModal,
        private providenciaService: ProvidenciaService,
        private activatedRoute: ActivatedRoute,
        private entidadService: EntidadService
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.cargarConfigMultiSelectAcciones();
        this.activatedRoute.data.subscribe(({ providencia }) => {
            this.providencia = providencia;

            if (this.providencia.id !== null && typeof this.providencia.id !== 'undefined') {
                // Cargar acciones asociadas a la providencia
                if (
                    this.providencia.instrucciones !== null &&
                    typeof this.providencia.instrucciones !== 'undefined' &&
                    this.providencia.instrucciones.length > 0
                ) {
                    this.cargarAccionesProvidencia(this.providencia.instrucciones);
                    this.activeModal.dismiss(true);
                    this.previousState();
                }
            }
        });

        this.entidadService.getAll().subscribe(
            (req: HttpResponse<IEntidad[]>) => {
                console.log('cargando entidades', req.body);
                this.entidades = req.body;
            },
            (req: HttpErrorResponse) => this.onError(req.message)
        );
        this.providencia.tipo = null;
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }
    cargarConfigMultiSelectAcciones() {
        this.dropdownListAcciones = [
            { id: 1, itemName: InstruccionesProvidencia.TOMAR_CONOCIMIENTO.split('_').join(' ') },
            { id: 2, itemName: InstruccionesProvidencia.CONVERSAR_CONMIGO.split('_').join(' ') },
            { id: 3, itemName: InstruccionesProvidencia.PROVIDENCIA_ARCHIVO.split('_').join(' ') },
            { id: 4, itemName: InstruccionesProvidencia.PROPONER_RESPUESTA_AL_DIRECTOR.split('_').join(' ') },
            { id: 5, itemName: InstruccionesProvidencia.PROPONER_DECRETO_O_RESOLUCION.split('_').join(' ') },
            { id: 6, itemName: InstruccionesProvidencia.ESTUDIAR_ANTECEDENTES_Y_PROCEDER_CONFORME_A_DERECHO.split('_').join(' ') },

        ];

        this.dropdownSettingsAcciones = {
            singleSelection: false,
            text: 'Seleccionar acciones',
            searchPlaceholderText: 'Busquedad' ,
            selectAllText: 'Seleccionar todas',
            unSelectAllText: 'Deseleccionar todas',
            enableSearchFilter: true,
            classes: 'myclass custom-class'
        };
    }

    /**
     * Método que permite cargar las acciones de una providencia que se va a editar.
     */
    cargarAccionesProvidencia(acciones: string[]) {
        acciones.map(accion => {
            this.dropdownListAcciones.map(a => {
                if (a.itemName === accion.split('_').join(' ')) {
                    this.selectedAcciones.push({ id: a.id, itemName: accion.split('_').join(' ') });
                }
            });
        });
    }

    previousState() {
        window.history.back();
    }
    save() {
        this.providencia.runImplicado = this.providencia.runImplicado.trim()
            .substring(0, this.providencia.runImplicado.trim().length - 1) + '-' + this.providencia.runImplicado
            .trim().substring(this.providencia.runImplicado.trim().length - 1);
        this.providencia.instrucciones = this.selectedAcciones.map(item => {
            return item.itemName.split(' ').join('_');
        });
        this.providencia.runSolicitante = this.providencia.runSolicitante.trim()
            .substring(0, this.providencia.runSolicitante.trim().length - 1) + '-' + this.providencia.runSolicitante
            .trim().substring(this.providencia.runSolicitante.trim().length - 1);
        this.providencia.instrucciones = this.selectedAcciones.map(item => {
            return item.itemName.split(' ').join('_');
        });

        this.isSaving = true;
        this.providencia.fechaSolicitud = moment(this.fechaSolicitud, DATE_FORMAT);
        this.providencia.fechaCreacion = moment(this.fechaCreacion, DATE_TIME_FORMAT);
        console.log('almacenando la providencia', this.providencia);
        if (this.providencia.id !== undefined) {
            this.subscribeToSaveResponse(this.providenciaService.update(this.providencia));
        } else {
            this.subscribeToSaveResponse(this.providenciaService.create(this.providencia));
        }
    }

    private subscribeToSaveResponse(result: Observable<HttpResponse<IProvidencia>>) {
        result.subscribe((res: HttpResponse<IProvidencia>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
    }

    private onSaveSuccess() {
        this.isSaving = false;
        this.previousState();
    }

    private onSaveError() {
        this.isSaving = false;
    }

    private onError(errorMessage: string) {
        this.jhiAlertService.error(errorMessage, null, null);
    }

    trackGrupoById(index: number, item: IGrupo) {
        return item.id;
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }
    get providencia() {
        return this._providencia;
    }

    set providencia(providencia: IProvidencia) {
        this._providencia = providencia;
        this.fechaSolicitud = moment(providencia.fechaSolicitud).format(DATE_FORMAT);
        this.fechaCreacion = moment(providencia.fechaCreacion).format(DATE_TIME_FORMAT);
    }

    onItemSelect(item: any) {}

    OnItemDeSelect(item: any) {}
    onSelectAll(items: any) {
        console.log(items);
    }
    onDeSelectAll(items: any) {
        console.log(items);
    }

    getUploadedAdjuntos($event) {
        this.providencia.adjuntos = $event;
    }
}
@Component({
    selector: 'jhi-providencia-update-popup',
    template: ''
})
export class ProvidenciaUpdatePopupComponent implements OnInit, OnDestroy {
    private ngbModalRef: NgbModalRef;

    constructor(private activatedRoute: ActivatedRoute, private router: Router, private modalService: NgbModal) {}

    ngOnInit() {
        this.activatedRoute.data.subscribe(({ providencia }) => {
            setTimeout(() => {
                this.ngbModalRef = this.modalService.open(ProvidenciaUpdateComponent as Component, {
                    size: 'lg',
                    backdrop: 'static'
                });
                this.ngbModalRef.componentInstance.providencia = providencia;
                this.ngbModalRef.result.then(
                    result => {
                        this.router.navigate([{ outlets: { popup: null } }], { replaceUrl: true, queryParamsHandling: 'merge' });
                        this.ngbModalRef = null;
                    },
                    reason => {
                        this.router.navigate([{ outlets: { popup: null } }], { replaceUrl: true, queryParamsHandling: 'merge' });
                        this.ngbModalRef = null;
                    }
                );
            }, 0);
        });
    }

    ngOnDestroy() {
        this.ngbModalRef = null;
    }
}
