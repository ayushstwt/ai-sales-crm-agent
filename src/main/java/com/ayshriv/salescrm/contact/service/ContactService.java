package com.ayshriv.salescrm.contact.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.contact.dto.ContactCreateRequest;
import com.ayshriv.salescrm.contact.dto.ContactSearchRequest;
import com.ayshriv.salescrm.contact.dto.ContactUpdateRequest;

public interface ContactService {

    ApiStatus listContacts(ContactSearchRequest request);

    ApiStatus viewContact(Long id);

    ApiStatus createContact(ContactCreateRequest request);

    ApiStatus editContact(Long id, ContactUpdateRequest request);

    ApiStatus deleteContact(Long id);
}
