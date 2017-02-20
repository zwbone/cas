package org.apereo.cas.adaptors.u2f.web.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.u2f.U2F;
import com.yubico.u2f.data.messages.RegisterRequest;
import com.yubico.u2f.data.messages.RegisterRequestData;
import org.apereo.cas.adaptors.u2f.U2FDeviceRegistrationRepository;
import org.apereo.cas.adaptors.u2f.U2FRegistration;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FStartRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FStartRegistrationAction extends AbstractAction {
    private final U2F u2f = new U2F();
    private final String serverAddress;
    private final U2FDeviceRegistrationRepository u2FDeviceRegistrationRepository;

    public U2FStartRegistrationAction(final String serverAddress, final U2FDeviceRegistrationRepository u2FDeviceRegistrationRepository) {
        this.serverAddress = serverAddress;
        this.u2FDeviceRegistrationRepository = u2FDeviceRegistrationRepository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();
        final RegisterRequestData registerRequestData = u2f.startRegistration(this.serverAddress,
                u2FDeviceRegistrationRepository.getRegistrations(p.getId()));
        u2FDeviceRegistrationRepository.getRequestStorage().put(registerRequestData.getRequestId(), registerRequestData.toJson());
        if (!registerRequestData.getRegisterRequests().isEmpty()) {
            final RegisterRequest req = registerRequestData.getRegisterRequests().iterator().next();
            requestContext.getFlowScope().put("u2fReg", new U2FRegistration(req.getChallenge(), req.getAppId()));
        }
        return success();
    }
}
