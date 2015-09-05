/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Identification Cards.
 *
 * FenixEdu Identification Cards is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Identification Cards is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Identification Cards.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.idcards.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.datacontract.schemas._2004._07.portalsantander_wcf.RegisterData;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.academic.ui.struts.action.person.PersonApplication.PersonalAreaApp;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.StrutsFunctionality;
import org.fenixedu.idcards.IdCardsConfiguration;

import pt.sibscartoes.portal.wcf.IRegistersInfo;
import pt.sibscartoes.portal.wcf.RegistersInfo;

import com.google.common.base.Strings;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;

@StrutsFunctionality(app = PersonalAreaApp.class, descriptionKey = "label.identification.card", path = "identification-card",
        titleKey = "label.identification.card")
@Mapping(module = "person", path = "/identificationCard")
@Forwards(@Forward(name = "show.card.information", path = "/person/identificationCard/showCardInformation.jsp"))
public class IdentificationCardDA extends Action {

    @Override
    public ActionForward execute(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final Person person = AccessControl.getPerson();
//        final String cardProdutionState =
//                CoreConfiguration.getConfiguration().developmentMode() ? "<Cannot read card state in development mode>" : getIdentificationCardState(person);

        final String cardProdutionState = getIdentificationCardState(person);

        request.setAttribute("person", person);
        request.setAttribute("state", cardProdutionState);
        return mapping.findForward("show.card.information");
    }

    private String getIdentificationCardState(Person person) {
        try {
//            ObjectFactory fazedorDeCenas = new ObjectFactory();

            final String userName = Strings.padEnd(person.getUsername(), 10, 'x');

            MemberSubmissionAddressingFeature feature = new MemberSubmissionAddressingFeature(true, true);

            IRegistersInfo client = new RegistersInfo(feature).getRegistersInfoWsHttp();
            setupClient((BindingProvider) client);
            RegisterData status = client.getRegister(userName);

            return status.getStatusDate().getValue().replaceAll("-", "/") + " : " + status.getStatus().getValue() + " - "
                    + status.getStatusDesc().getValue();
        } finally {
        }
    }

    protected void setupClient(BindingProvider bindingProvider) {
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "https://portal.sibscartoes.pt/wcf/RegistersInfo.svc");

        bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
                IdCardsConfiguration.getConfiguration().sibsWebServiceUsername());
        bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
                IdCardsConfiguration.getConfiguration().sibsWebServicePassword());
    }

//
//    private String x(Person person) {
//
//        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
//
//        factory.setServiceClass(IRegistersInfo.class);
//        factory.setAddress("https://portal.sibscartoes.pt/wcf/RegistersInfo.svc");
//        factory.setBindingId("http://schemas.xmlsoap.org/wsdl/soap12/");
//        factory.getFeatures().add(new WSAddressingFeature());
//        IRegistersInfo port = (IRegistersInfo) factory.create();
//
//        /*define WSDL policy*/
//        Client client = ClientProxy.getClient(port);
//        HTTPConduit http = (HTTPConduit) client.getConduit();
//        http.getAuthorization().setUserName(IdCardsConfiguration.getConfiguration().sibsWebServiceUsername());
//        http.getAuthorization().setPassword(IdCardsConfiguration.getConfiguration().sibsWebServicePassword());
//
//        final String userName = Strings.padEnd(person.getUsername(), 10, 'x');
//        RegisterData statusInformation = port.getRegister(userName);
//
//        return statusInformation.getStatusDate().getValue().replaceAll("-", "/") + " : "
//                + statusInformation.getStatus().getValue() + " - " + statusInformation.getStatusDesc().getValue();
//
//    }
}
