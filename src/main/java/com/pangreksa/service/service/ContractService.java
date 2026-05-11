package com.pangreksa.service.service;

import com.pangreksa.service.model.entity.*;
import com.pangreksa.service.model.enumerate.ContractStatusEnum;
import com.pangreksa.service.model.enumerate.ContractTypeEnum;
import com.pangreksa.service.model.repo.HrContractRepository;
import com.pangreksa.service.model.repo.HrNotificationRepository;
import com.pangreksa.service.shared.security.AppUserInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Service
public class ContractService {

    private final HrContractRepository contractRepository;
    private final HrNotificationRepository notificationRepository;

    public ContractService(HrContractRepository contractRepository,
                           HrNotificationRepository notificationRepository) {

        this.contractRepository = contractRepository;
        this.notificationRepository = notificationRepository;
    }

    // =====================================================
    // CREATE DRAFT
    // =====================================================
    public HrContract createDraft(HrContract contract,
                                  FwAppUser currentUser) {

        validateActiveContract(contract);

        contract.setStatus(ContractStatusEnum.DRAFT);

        contract.setContractNumber(
                generateContractNumber(contract.getContractType())
        );

        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());

        return contractRepository.save(contract);
    }

    // =====================================================
    // SUBMIT APPROVAL
    // =====================================================
    public HrContract submitApproval(HrContract contract,
                                     FwAppUser currentUser) {

        contract.setStatus(ContractStatusEnum.WAITING_APPROVAL);

        contract.setUpdatedAt(LocalDateTime.now());

        HrContract saved = contractRepository.save(contract);

        // notif manager
        if (saved.getSubmittedTo() != null) {

            createNotification(
                    saved.getSubmittedTo().getFirstName(),
                    "Kontrak karyawan menunggu approval",
                    "CONTRACT",
                    saved.getId()
            );
        }

        return saved;
    }

    // =====================================================
    // APPROVE CONTRACT
    // =====================================================
    public HrContract approveContract(HrContract contract,
                                      FwAppUser approver) {

        contract.setStatus(ContractStatusEnum.APPROVED);

        contract.setApprovedAt(LocalDateTime.now());

        contract.setApprovedBy(approver.getPerson());

        contract.setUpdatedAt(LocalDateTime.now());

        HrContract saved = contractRepository.save(contract);

        // notif HR
        createNotification(
                contract.getCreatedBy().toString(),
                "Kontrak telah disetujui manager",
                "CONTRACT",
                contract.getId()
        );

        return saved;
    }

    // =====================================================
    // ACTIVATE CONTRACT
    // =====================================================
    public HrContract activateContract(HrContract contract,
                                       FwAppUser currentUser) {

        validateOnlyOneActiveContract(contract);

        contract.setStatus(ContractStatusEnum.ACTIVE);

        contract.setUpdatedAt(LocalDateTime.now());

        return contractRepository.save(contract);
    }

    // =====================================================
    // TERMINATE CONTRACT
    // =====================================================
    public HrContract terminateContract(HrContract contract,
                                        FwAppUser currentUser) {

        contract.setStatus(ContractStatusEnum.TERMINATED);

        contract.setUpdatedAt(LocalDateTime.now());

        return contractRepository.save(contract);
    }

    // =====================================================
    // RENEW CONTRACT
    // =====================================================
    public HrContract renewContract(HrContract oldContract,
                                    HrContract newContract,
                                    FwAppUser currentUser) {

        oldContract.setStatus(ContractStatusEnum.RENEWED);

        contractRepository.save(oldContract);

        newContract.setPreviousContractId(oldContract.getId());

        return createDraft(newContract, currentUser);
    }

    // =====================================================
    // VALIDATION
    // =====================================================
    private void validateOnlyOneActiveContract(HrContract contract) {

        List<HrContract> activeContracts =
                contractRepository.findByPersonAndStatus(
                        contract.getPerson(),
                        ContractStatusEnum.ACTIVE
                );

        boolean hasAnotherActive =
                activeContracts.stream()
                        .anyMatch(c -> !c.getId().equals(contract.getId()));

        if (hasAnotherActive) {
            throw new RuntimeException(
                    "Karyawan masih memiliki kontrak aktif"
            );
        }
    }

    private void validateActiveContract(HrContract contract) {

        if (contract.getStatus() == ContractStatusEnum.ACTIVE) {
            validateOnlyOneActiveContract(contract);
        }
    }

    // =====================================================
    // CONTRACT NUMBER
    // =====================================================
    private String generateContractNumber(ContractTypeEnum type) {

        long count = contractRepository.count() + 1;

        String running =
                String.format("%03d", count);

        String month =
                toRoman(LocalDate.now().getMonth());

        String year =
                String.valueOf(LocalDate.now().getYear());

        return running
                + "/FST/"
                + type.name()
                + "/"
                + month
                + "/"
                + year;
    }

    private String toRoman(Month month) {

        return switch (month.getValue()) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            case 11 -> "XI";
            case 12 -> "XII";
            default -> "";
        };
    }

    // =====================================================
    // NOTIFICATION
    // =====================================================
    private void createNotification(String username,
                                    String title,
                                    String type,
                                    Long referenceId) {

        HrNotification notif = new HrNotification();

        notif.setUsername(username);

        notif.setTitle(title);

        notif.setType(type);

        notif.setReferenceId(referenceId);

        notif.setIsRead(false);

        notif.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notif);
    }

    public List<HrContract> findAll() {
        return contractRepository.findAll();
    }

    public List<HrContract> findByStatus(
            ContractStatusEnum status
    ) {
        return contractRepository.findByStatus(status);
    }
}