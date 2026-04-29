package com.pangreksa.service.shared;

import lombok.Setter;

/**
 * Per-user, per-page CRUD permissions resolved from the framework
 * responsibility tables. Adapter UIs (Vaadin views, REST endpoints) consult
 * this to decide which actions to expose.
 */
@Setter
public class Authorization {
    public boolean canView = false;
    public boolean canCreate = false;
    public boolean canEdit = false;
    public boolean canDelete = false;

    public Authorization(boolean canView, boolean canCreate, boolean canEdit, boolean canDelete) {
        this.canView = canView;
        this.canCreate = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }
}
