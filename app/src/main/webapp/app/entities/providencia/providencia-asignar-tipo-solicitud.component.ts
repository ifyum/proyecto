import { Component, OnDestroy, OnInit } from '@angular/core';
import { IProvidencia, IProvidenciaUpdateTipoSolicitud } from 'app/shared/model/providencia.model';
import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiEventManager } from 'ng-jhipster';
import { ProvidenciaService } from 'app/entities/providencia/providencia.service';
import { HttpErrorResponse, HttpResponse} from '@angular/common/http';

@Component({
    selector: 'jhi-providencia-asignar-tipo-solicitud',
    templateUrl: './providencia-asignar-tipo-solicitud.component.html'
})
export class ProvidenciaAsignarTipoSolicitudComponent implements OnInit {
    providencia: IProvidencia;
    private _providencia: IProvidencia;

    providenciaUpdateTipoSolicitud: IProvidenciaUpdateTipoSolicitud = {
        providenciaId: null,
        tipoSolicitud: null
    };

    constructor(
        public activeModal: NgbActiveModal,
        private router: Router,
        private eventManager: JhiEventManager,
        private providenciaService: ProvidenciaService
    ) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    ngOnInit() {
        this.providenciaUpdateTipoSolicitud.providenciaId = this.providencia.id;
        console.log('esta es la providencia ', this.providencia.id);
    }

    get providencias() {
        return this._providencia;
    }

    set providencias(providencia: IProvidencia) {
        this._providencia = providencia;
    }

    asignarTipo() {
        this.providenciaService.updateTipoSolicitud(this.providenciaUpdateTipoSolicitud)
            .subscribe(
                (req: HttpResponse<IProvidencia>) => {
                    this.activeModal.close(req.body);
                },
                (req: HttpErrorResponse) => {

                }
            );
    }

    previousState() {
        window.history.back();
    }

    private onError(errorMessage: string) {
        console.log(errorMessage);
    }
}

@Component({
    selector: 'jhi-providencia-asignar-tipo-solicitud-popup',
    template: ''
})
export class  ProvidenciaAsignarTipoSolicitudPopupComponent implements OnInit, OnDestroy {
    private ngbModalRef: NgbModalRef;

    constructor(private activatedRoute: ActivatedRoute, private router: Router, private modalService: NgbModal) {}

    ngOnInit() {
        this.activatedRoute.data.subscribe(({ providencia }) => {
            setTimeout(() => {
                this.ngbModalRef = this.modalService.open(ProvidenciaAsignarTipoSolicitudComponent as Component, {
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
