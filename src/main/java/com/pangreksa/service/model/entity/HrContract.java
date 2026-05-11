package com.pangreksa.service.model.entity;

import com.pangreksa.service.model.enumerate.ContractStatusEnum;
import com.pangreksa.service.model.enumerate.ContractTypeEnum;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_contract", schema = "public")
public class HrContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number")
    private String contractNumber;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private HrPerson person;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private HrCompany company;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private ContractTypeEnum contractType;

    @Enumerated(EnumType.STRING)
    private ContractStatusEnum status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @Column(name = "notes")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private HrPerson approvedBy;

    @ManyToOne
    @JoinColumn(name = "submitted_to")
    private HrPerson submittedTo;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "previous_contract_id")
    private Long previousContractId;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private FwAppUser createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private FwAppUser updatedBy;



    // ===== GETTER SETTER =====

    public Long getId() {
        return id;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public HrPerson getPerson() {
        return person;
    }

    public void setPerson(HrPerson person) {
        this.person = person;
    }

    public HrCompany getCompany() {
        return company;
    }

    public void setCompany(HrCompany company) {
        this.company = company;
    }

    public ContractTypeEnum getContractType() {
        return contractType;
    }

    public void setContractType(ContractTypeEnum contractType) {
        this.contractType = contractType;
    }

    public ContractStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ContractStatusEnum status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public HrPerson getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(HrPerson approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public HrPerson getSubmittedTo() {
        return submittedTo;
    }

    public void setSubmittedTo(HrPerson submittedTo) {
        this.submittedTo = submittedTo;
    }

    public Long getPreviousContractId() {
        return previousContractId;
    }

    public void setPreviousContractId(Long previousContractId) {
        this.previousContractId = previousContractId;
    }

    public FwAppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(FwAppUser createdBy) {
        this.createdBy = createdBy;
    }

    public FwAppUser getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(FwAppUser updatedBy) {
        this.updatedBy = updatedBy;
    }

}