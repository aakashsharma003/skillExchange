package com.skillexchange.model.dto;

import com.skillexchange.model.UserDetails;
import com.skillexchange.model.request.ExchangeRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRequestCardDTO {

        private ExchangeRequest request;
        private UserDetails senderDetails;
        private UserDetails receiverDetails;
}
