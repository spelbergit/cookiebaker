/*
Copyright 2020 Spelberg IT

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package nl.cookiebaker.core;

import java.util.Random;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RealBakerRecipeTest {

    @Test
    public void testWholeRecipe() {
        CookieBaker cookieBaker = new CookieBaker();

        cookieBaker.provide(new OrderRecipe());
        Object lastEvent = cookieBaker.doe(new OrderPlacedEvent(new Order(13)));
        assertEquals(new GoodsManufacturedEvent(new Goods("Playmobil")), lastEvent);

        lastEvent = cookieBaker.doe(new CustomerInfoReceivedEvent(new CustomerInfo("Jansen", "Invoice Address", "Shipping Address")));
        assertEquals(new InvoiceWasSentEvent("Jansen", "Invoice Address"), lastEvent);


    }


    private static class OrderRecipe {

        @Interaction(OrderPlacedEvent.class)
        public Object validateOrder(@Ingredient("order") Order order) {
            if (order.orderId > 42) {
                return new SorryEvent();
            } else {
                return new ValidEvent();
            }
        }

        @Interaction({ValidEvent.class, PaymentMadeEvent.class})
        public Object manufactureGoods(@Ingredient("order") Order order) {
            return new GoodsManufacturedEvent(new Goods("Playmobil"));
        }

        @Interaction(CustomerInfoReceivedEvent.class)
        public Object shipGoods(@Ingredient("goods") Goods goods, @Ingredient("customerInfo") CustomerInfo customerInfo) {
            return new GoodsShippedEvent(String.valueOf(new Random().nextInt()), customerInfo.getShippingAddress());
        }

        @Interaction(GoodsShippedEvent.class)
        public Object sendInvoice(@Ingredient("customerInfo") CustomerInfo customerInfo, @Ingredient("goods") Goods goods) {
            return new InvoiceWasSentEvent(customerInfo.name, customerInfo.invoiceAddress);
        }
    }


    @Data
    @Event
    private static class OrderPlacedEvent {
        @Ingredient("order")
        @NonNull
        private Order order;
    }

    @Data
    @Event
    private static class SorryEvent {
    }

    @Data
    @Event
    private static class ValidEvent {
    }

    @Data
    @Event
    private static class PaymentMadeEvent {
    }

    @Data
    @Event
    private static class CustomerInfoReceivedEvent {
        @Ingredient("customerInfo")
        @NonNull
        private CustomerInfo customerInfo;
    }

    @Data
    @Event
    private static class GoodsManufacturedEvent {
        @Ingredient("goods")
        @NonNull
        private Goods goods;
    }

    @Data
    @Event
    private static class GoodsShippedEvent {
        @Ingredient("trackingId")
        @NonNull
        private String trackingId;

        @Ingredient("shippingAddress")
        @NonNull
        private String shippingAddress;
    }

    @Data
    @Event
    private static class InvoiceWasSentEvent {
        @NonNull
        private String to;
        @NonNull
        private String address;
    }

    @Data
    private static class Order {
        @NonNull
        private int orderId;
    }

    @Data
    private static class CustomerInfo {
        @NonNull
        private final String name;
        @NonNull
        private final String invoiceAddress;
        @NonNull
        private final String shippingAddress;
    }

    @Data
    private static class Goods {
        @NonNull
        private final String item;
    }


}
