import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiResolvePagingParams } from 'ng-jhipster';
import { UserRouteAccessService } from 'app/core';
import { of } from 'rxjs';
import { map } from 'rxjs/operators';
import { IProvidencia, Providencia } from 'app/shared/model/providencia.model';
import { ProvidenciaService } from './providencia.service';
import { ProvidenciaComponent } from './providencia.component';
import { ProvidenciaUpdateComponent } from './providencia-update.component';
import { ProvidenciaDeletePopupComponent } from './providencia-delete-dialog.component';
import { ProvidenciaResponderPopupComponent } from 'app/entities/providencia/providencia-responder-dialog.component';
import { ProvidenciaDevolverPopupComponent } from 'app/entities/providencia/providencia-devolver-dialog.component';
import { ProvidenciaDetailComponent } from 'app/entities/providencia/providencia-detail.component';
import { ProvidenciaDetailDgdpComponent } from './providencia-detail-dgdp.component';
import { ProvidenciaAsignarNumeroResolucionPopupComponent } from 'app/entities/providencia/providencia-asignar-numero-resolucion.component';
import { ProvidenciaAsignarFiscalPopupComponent } from 'app/entities/providencia/providencia-asignar-fiscal.component';
import { ProvidenciaRelacionarPopupComponent } from './providencia-relacionar-dialog.component';
import { ProvidenciaAsignarNumeroReferenciaPopupComponent } from 'app/entities/providencia/providencia-asignar-numero-referencia.component';
import { ProvidenciaAsignarTipoSolicitudPopupComponent } from 'app/entities/providencia/providencia-asignar-tipo-solicitud.component';
import {ProvidenciaFiscalAceptaPopupComponent} from 'app/entities/providencia/providencia-fiscal-acepta.component';
import {ProvidenciaFiscalRechazaPopupComponent} from 'app/entities/providencia/providencia-fiscal-rechaza.component';
import {ProvidenciaFiscalProrrogaPopupComponent} from 'app/entities/providencia/providencia-fiscal-prorroga.component';
import {ProvidenciaApelaPopupComponent} from 'app/entities/providencia/providencia-apela.component';
import {ProvidenciaNoApelaPopupComponent} from 'app/entities/providencia/providencia-no-apela.component';

@Injectable({ providedIn: 'root' })
export class ProvidenciaResolve implements Resolve<IProvidencia> {
    constructor(private service: ProvidenciaService) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const id = route.params['id'] ? route.params['id'] : null;
        if (id) {
            return this.service.find(id).pipe(map((providencia: HttpResponse<Providencia>) => providencia.body));
        }
        return of(new Providencia());
    }
}

export const providenciaRoute: Routes = [
    {
        path: 'providencia',
        component: ProvidenciaComponent,
        resolve: {
            pagingParams: JhiResolvePagingParams
        },
        data: {
            authorities: ['ROLE_USER', 'VISUALIZAR_PROVIDENCIA'],
            defaultSort: 'id,asc',
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'providencia/:id/view',
        component: ProvidenciaDetailComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'VISUALIZAR_PROVIDENCIA'],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'providencia/:id/viewDgdp',
        component: ProvidenciaDetailDgdpComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: [],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'providencia/new',
        component: ProvidenciaUpdateComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'CREAR_PROVIDENCIA'],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'providencia/:id/edit',
        component: ProvidenciaUpdateComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'EDITAR_PROVIDENCIA'],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService]
    }
];

export const providenciaPopupRoute: Routes = [
    {
        path: 'providencia/:id/delete',
        component: ProvidenciaDeletePopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/responder',
        component: ProvidenciaResponderPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'DERIVAR_PROVIDENCIA'],
            pageTitle: 'Responder providencia',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/fiscalAcepta',
        component: ProvidenciaFiscalAceptaPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'DERIVAR_PROVIDENCIA'],
            pageTitle: 'Aceptar providencia'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/fiscalRechaza',
        component: ProvidenciaFiscalRechazaPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'DERIVAR_PROVIDENCIA'],
            pageTitle: 'Rechazar providencia'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/prorroga',
        component: ProvidenciaFiscalProrrogaPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'DERIVAR_PROVIDENCIA'],
            pageTitle: 'prorroga'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },

    {
        path: 'providencia/:id/apela',
        component: ProvidenciaApelaPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'DERIVAR_PROVIDENCIA'],
            pageTitle: 'Apela'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/noApela',
        component: ProvidenciaNoApelaPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'DERIVAR_PROVIDENCIA'],
            pageTitle: 'NO Apela'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/devolver',
        component: ProvidenciaDevolverPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: ['ROLE_USER', 'DERIVAR_PROVIDENCIA'],
            pageTitle: 'Responder providencia'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/asignarFiscal',
        component: ProvidenciaAsignarFiscalPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: [],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/numerarResolucion',
        component: ProvidenciaAsignarNumeroResolucionPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: [],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/numerarReferencia',
        component: ProvidenciaAsignarNumeroReferenciaPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: [],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService],
        runGuardsAndResolvers: 'always',
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/tipoSolicitud',
        component: ProvidenciaAsignarTipoSolicitudPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: [],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService],
        runGuardsAndResolvers: 'always',
        outlet: 'popup'
    },
    {
        path: 'providencia/:id/relacionar',
        component: ProvidenciaRelacionarPopupComponent,
        resolve: {
            providencia: ProvidenciaResolve
        },
        data: {
            authorities: [],
            pageTitle: 'pdisciplinarioApp.providencia.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    }
];
