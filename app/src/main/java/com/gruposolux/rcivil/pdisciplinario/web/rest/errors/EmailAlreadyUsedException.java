package com.gruposolux.rcivil.pdisciplinario.web.rest.errors;

public class EmailAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyUsedException() {
        super(ErrorConstants.EMAIL_ALREADY_USED_TYPE, "\n" + "Correo electrónico ya está en uso!", "userManagement", "emailexists");
    }
}
