package com.ayshriv.salescrm.ai.entity;

import com.ayshriv.salescrm.common.entity.BaseEntity;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
public class Conversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConversationMessage> messages = new ArrayList<>();

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "pending_action_status", length = 50)
    private PendingActionStatus pendingActionStatus = PendingActionStatus.NONE;

    @Column(name = "pending_action_type", length = 100)
    private String pendingActionType;

    @Column(name = "pending_action_payload", columnDefinition = "TEXT")
    private String pendingActionPayload;

    @Column(name = "pending_action_description", columnDefinition = "TEXT")
    private String pendingActionDescription;

    @Column(name = "pending_action_created_on")
    private LocalDateTime pendingActionCreatedOn;

    public Conversation() {
    }

    public Conversation(Organization organization, User user, String title) {
        this.organization = organization;
        this.user = user;
        this.title = title;
    }

    @PrePersist
    protected void onCreate() {
        if (createdOn == null) {
            createdOn = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedOn = LocalDateTime.now();
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public List<ConversationMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConversationMessage> messages) {
        this.messages = messages;
    }

    public PendingActionStatus getPendingActionStatus() {
        return pendingActionStatus;
    }

    public void setPendingActionStatus(PendingActionStatus pendingActionStatus) {
        this.pendingActionStatus = pendingActionStatus;
    }

    public String getPendingActionType() {
        return pendingActionType;
    }

    public void setPendingActionType(String pendingActionType) {
        this.pendingActionType = pendingActionType;
    }

    public String getPendingActionPayload() {
        return pendingActionPayload;
    }

    public void setPendingActionPayload(String pendingActionPayload) {
        this.pendingActionPayload = pendingActionPayload;
    }

    public String getPendingActionDescription() {
        return pendingActionDescription;
    }

    public void setPendingActionDescription(String pendingActionDescription) {
        this.pendingActionDescription = pendingActionDescription;
    }

    public LocalDateTime getPendingActionCreatedOn() {
        return pendingActionCreatedOn;
    }

    public void setPendingActionCreatedOn(LocalDateTime pendingActionCreatedOn) {
        this.pendingActionCreatedOn = pendingActionCreatedOn;
    }

    public boolean hasPendingConfirmation() {
        return PendingActionStatus.PENDING.equals(this.pendingActionStatus);
    }

    public void clearPendingDetails() {
        this.pendingActionType = null;
        this.pendingActionPayload = null;
        this.pendingActionDescription = null;
        this.pendingActionCreatedOn = null;
    }

    public void clearPendingAction() {
        this.pendingActionStatus = PendingActionStatus.NONE;
        clearPendingDetails();
    }
}
