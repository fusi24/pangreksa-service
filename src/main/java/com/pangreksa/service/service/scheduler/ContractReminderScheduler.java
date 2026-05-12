package com.pangreksa.service.scheduler;

import com.pangreksa.service.model.entity.HrContract;
import com.pangreksa.service.model.entity.HrNotification;
import com.pangreksa.service.model.enumerate.ContractStatusEnum;
import com.pangreksa.service.model.repo.HrContractRepository;
import com.pangreksa.service.model.repo.HrNotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ContractReminderScheduler {

    private final HrContractRepository contractRepository;
    private final HrNotificationRepository notificationRepository;

    public ContractReminderScheduler(
            HrContractRepository contractRepository,
            HrNotificationRepository notificationRepository
    ) {

        this.contractRepository = contractRepository;
        this.notificationRepository = notificationRepository;
    }

    // =====================================================
    // RUN EVERY DAY 08:00
    // =====================================================

    @Scheduled(cron = "0 0 8 * * *")
    public void checkContractExpiration() {

        List<HrContract> contracts =
                contractRepository.findAll();

        for (HrContract contract : contracts) {

            if (contract.getEndDate() == null) {
                continue;
            }

            long days =
                    ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            contract.getEndDate()
                    );

            // =============================================
            // EXPIRED
            // =============================================

            if (days < 0) {

                contract.setStatus(
                        ContractStatusEnum.EXPIRED
                );

                contractRepository.save(contract);

                createNotification(
                        contract,
                        "Kontrak telah berakhir"
                );

                continue;
            }

            // =============================================
            // H-30
            // =============================================

            if (days <= 30) {

                contract.setStatus(
                        ContractStatusEnum.EXPIRING_SOON
                );

                contractRepository.save(contract);

                createNotification(
                        contract,
                        "Kontrak akan habis dalam 30 hari"
                );

                continue;
            }

            // =============================================
            // H-60
            // =============================================

            if (days <= 60) {

                contract.setStatus(
                        ContractStatusEnum.EXPIRING_SOON
                );

                contractRepository.save(contract);

                createNotification(
                        contract,
                        "Kontrak akan habis dalam 60 hari"
                );
            }
        }
    }

    // =====================================================
    // NOTIFICATION
    // =====================================================

    private void createNotification(
            HrContract contract,
            String message
    ) {

        HrNotification notif =
                new HrNotification();

        notif.setUsername(
                contract.getSubmittedTo()
                        .getFirstName()
        );

        notif.setTitle(
                message
                        + " : "
                        + contract.getPerson()
                        .getFirstName()
        );

        notif.setType("CONTRACT");

        notif.setReferenceId(
                contract.getId()
        );

        notif.setIsRead(false);

        notif.setCreatedAt(
                LocalDateTime.now()
        );

        notificationRepository.save(notif);
    }
}