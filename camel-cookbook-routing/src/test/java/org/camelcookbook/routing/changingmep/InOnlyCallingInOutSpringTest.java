/*
 * Copyright (C) Scott Cranton and Jakub Korab
 * https://github.com/CamelCookbook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camelcookbook.routing.changingmep;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InOnlyCallingInOutSpringTest extends CamelSpringTestSupport {

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("spring/changingMep-inOnlyCallingInOut-context.xml");
    }

    @Produce(uri = "direct:in")
    protected ProducerTemplate template;

    @EndpointInject(uri = "mock:modifyMessage")
    private MockEndpoint modifyMessage;

    @EndpointInject(uri = "mock:afterMessageModified")
    private MockEndpoint afterMessageModified;

    @Test
    public void testMEPChangedForModifyMessage() throws InterruptedException {
        String messageBody = "message";

        modifyMessage.setExpectedMessageCount(1);
        modifyMessage.message(0).body().isEqualTo(messageBody);
        modifyMessage.message(0).exchangePattern().isEqualTo(ExchangePattern.InOut);

        afterMessageModified.setExpectedMessageCount(1);
        afterMessageModified.message(0).body().isEqualTo("[message] has been modified!");
        // the exchange pattern has now been modified for the remainder of the route
        afterMessageModified.message(0).exchangePattern().isEqualTo(ExchangePattern.InOut);

        template.requestBody(messageBody);

        assertMockEndpointsSatisfied();
        Exchange modifyMessageExchange = modifyMessage.getReceivedExchanges().get(0);
        Exchange afterMessageModifiedExchange = afterMessageModified.getReceivedExchanges().get(0);

        // these are not the same exchange objects
        assertNotEquals(modifyMessageExchange, afterMessageModifiedExchange);

        // the transactions are the same
        assertEquals(modifyMessageExchange.getUnitOfWork(), afterMessageModifiedExchange.getUnitOfWork());
    }

}