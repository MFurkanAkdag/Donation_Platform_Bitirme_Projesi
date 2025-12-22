package com.seffafbagis.api.entity.organization;

import com.seffafbagis.api.enums.OrganizationType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationEntityTest {

    @Test
    void addContact_ShouldSetBidirectionalRelationship() {
        // Arrange
        Organization org = new Organization();
        org.setContacts(new ArrayList<>());

        OrganizationContact contact = new OrganizationContact();

        // Act
        org.addContact(contact);

        // Assert
        assertThat(org.getContacts()).contains(contact);
        assertThat(contact.getOrganization()).isEqualTo(org);
    }

    @Test
    void removeContact_ShouldClearBidirectionalRelationship() {
        // Arrange
        Organization org = new Organization();
        org.setContacts(new ArrayList<>());
        OrganizationContact contact = new OrganizationContact();
        org.addContact(contact);

        // Act
        org.removeContact(contact);

        // Assert
        assertThat(org.getContacts()).doesNotContain(contact);
        assertThat(contact.getOrganization()).isNull();
    }

    @Test
    void entity_ShouldHaveCorrectType() {
        Organization org = new Organization();
        org.setOrganizationType(OrganizationType.FOUNDATION);
        assertThat(org.getOrganizationType()).isEqualTo(OrganizationType.FOUNDATION);
    }
}
