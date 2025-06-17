-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema kedu_project_manager
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema kedu_project_manager
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `kedu_project_manager` ;
USE `kedu_project_manager` ;

-- -----------------------------------------------------
-- Table `timestamps`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `timestamps` (
                                            `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                            `update_time` TIMESTAMP NULL);


-- -----------------------------------------------------
-- Table `timestamps_1`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `timestamps_1` (
                                              `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                              `update_time` TIMESTAMP NULL);


-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
                                      `username` VARCHAR(16) NOT NULL,
                                      `email` VARCHAR(255) NULL,
                                      `password` VARCHAR(32) NOT NULL,
                                      `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP);


-- -----------------------------------------------------
-- Table `app_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_user` (
                                          `id` BINARY(16) NOT NULL,
                                          `email` VARCHAR(63) NOT NULL,
                                          `password` VARCHAR(63) NULL,
                                          `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
                                          `updated_at` TIMESTAMP NOT NULL DEFAULT NOW(),
                                          `active` BIT(1) NOT NULL DEFAULT 1,
                                          PRIMARY KEY (`id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `email`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `email` (
                                       `address` VARCHAR(63) NOT NULL,
                                       PRIMARY KEY (`address`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `admin`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `admin` (
                                       `usr_id` BINARY(16) NOT NULL,
                                       `email` VARCHAR(63) NOT NULL,
                                       PRIMARY KEY (`usr_id`),
                                       CONSTRAINT `amn_usr_fk`
                                           FOREIGN KEY (`usr_id`)
                                               REFERENCES `app_user` (`id`)
                                               ON DELETE NO ACTION
                                               ON UPDATE NO ACTION,
                                       CONSTRAINT `amn_email_fk`
                                           FOREIGN KEY (`email`)
                                               REFERENCES `email` (`address`)
                                               ON DELETE NO ACTION
                                               ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE INDEX `amn_email_fk_idx` ON `admin` (`email` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `manager`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `manager` (
                                         `usr_id` BINARY(16) NOT NULL,
                                         `created_by` BINARY(16) NOT NULL,
                                         `email` VARCHAR(63) NOT NULL,
                                         PRIMARY KEY (`usr_id`),
                                         CONSTRAINT `mnr_amn_fk`
                                             FOREIGN KEY (`created_by`)
                                                 REFERENCES `admin` (`usr_id`)
                                                 ON DELETE NO ACTION
                                                 ON UPDATE NO ACTION,
                                         CONSTRAINT `mnr_usr_fk`
                                             FOREIGN KEY (`usr_id`)
                                                 REFERENCES `app_user` (`id`)
                                                 ON DELETE NO ACTION
                                                 ON UPDATE NO ACTION,
                                         CONSTRAINT `mnr_email_fk`
                                             FOREIGN KEY (`email`)
                                                 REFERENCES `email` (`address`)
                                                 ON DELETE NO ACTION
                                                 ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE INDEX `manager_admin_fk_idx` ON `manager` (`created_by` ASC) VISIBLE;

CREATE INDEX `mnr_email_fk_idx` ON `manager` (`email` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `academic_quarter`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `academic_quarter` (
                                                  `number` INT NOT NULL,
                                                  PRIMARY KEY (`number`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `academic_group`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `academic_group` (
                                                `name` CHAR(1) NOT NULL,
                                                PRIMARY KEY (`name`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quarter_group`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quarter_group` (
                                               `agp_name` CHAR(1) NOT NULL,
                                               `aqr_number` INT NOT NULL,
                                               PRIMARY KEY (`agp_name`, `aqr_number`),
                                               CONSTRAINT `qgp_aqr_fk`
                                                   FOREIGN KEY (`aqr_number`)
                                                       REFERENCES `academic_quarter` (`number`)
                                                       ON DELETE NO ACTION
                                                       ON UPDATE NO ACTION,
                                               CONSTRAINT `qgp_agp_fk`
                                                   FOREIGN KEY (`agp_name`)
                                                       REFERENCES `academic_group` (`name`)
                                                       ON DELETE NO ACTION
                                                       ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `student`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `student` (
                                         `usr_id` BINARY(16) NOT NULL,
                                         `created_by` BINARY(16) NOT NULL,
                                         `gpg_name` CHAR(1) NOT NULL,
                                         `qgp_number` INT NOT NULL,
                                         `email` VARCHAR(63) NOT NULL,
                                         `first_name` VARCHAR(45) NULL,
                                         `last_name` VARCHAR(45) NULL,
                                         PRIMARY KEY (`usr_id`),
                                         CONSTRAINT `sdt_usr_fk`
                                             FOREIGN KEY (`usr_id`)
                                                 REFERENCES `app_user` (`id`)
                                                 ON DELETE NO ACTION
                                                 ON UPDATE NO ACTION,
                                         CONSTRAINT `std_mnr_fk`
                                             FOREIGN KEY (`created_by`)
                                                 REFERENCES `manager` (`usr_id`)
                                                 ON DELETE NO ACTION
                                                 ON UPDATE NO ACTION,
                                         CONSTRAINT `sdt_email_fk`
                                             FOREIGN KEY (`email`)
                                                 REFERENCES `email` (`address`)
                                                 ON DELETE NO ACTION
                                                 ON UPDATE NO ACTION,
                                         CONSTRAINT `std_qgp_fk`
                                             FOREIGN KEY (`qgp_number` , `gpg_name`)
                                                 REFERENCES `quarter_group` (`aqr_number` , `agp_name`)
                                                 ON DELETE NO ACTION
                                                 ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE INDEX `std_mnr_fk_idx` ON `student` (`created_by` ASC) VISIBLE;

CREATE UNIQUE INDEX `email_UNIQUE` ON `student` (`email` ASC) VISIBLE;

CREATE INDEX `std_qgp_fk_idx` ON `student` (`qgp_number` ASC, `gpg_name` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `board`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `board` (
                                       `mnr_id` BINARY(16) NOT NULL,
                                       `title` VARCHAR(30) NOT NULL,
                                       `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
                                       PRIMARY KEY (`mnr_id`),
                                       CONSTRAINT `brd_mnr_id`
                                           FOREIGN KEY (`mnr_id`)
                                               REFERENCES `manager` (`usr_id`)
                                               ON DELETE NO ACTION
                                               ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `color`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `color` (
                                       `name` ENUM('Red', 'Orange', 'Amber', 'Yellow', 'Lime', 'Green', 'Emerald', 'Teal', 'Cyan', 'Blue', 'Indigo', 'Violet', 'Fuchsia', 'Pink', 'Rose', 'Gray', 'Slate', 'Zinc') NOT NULL,
                                       `code` BINARY(16) NOT NULL,
                                       PRIMARY KEY (`name`))
    ENGINE = InnoDB;

CREATE UNIQUE INDEX `code_UNIQUE` ON `color` (`code` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `task`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `task` (
                                      `id` BINARY(16) NOT NULL,
                                      `title` VARCHAR(63) NOT NULL,
                                      `description` VARCHAR(255) NOT NULL,
                                      `clr_name` ENUM('Red', 'Orange', 'Amber', 'Yellow', 'Lime', 'Green', 'Emerald', 'Teal', 'Cyan', 'Blue', 'Indigo', 'Violet', 'Fuchsia', 'Pink', 'Rose', 'Gray', 'Slate', 'Zinc') NOT NULL,
                                      `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      `started_at` TIMESTAMP NULL,
                                      `finished_at` TIMESTAMP NULL,
                                      PRIMARY KEY (`id`),
                                      CONSTRAINT `tsk_clr_fk`
                                          FOREIGN KEY (`clr_name`)
                                              REFERENCES `color` (`name`)
                                              ON DELETE NO ACTION
                                              ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE INDEX `tsk_clr_fk_idx` ON `task` (`clr_name` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `stage`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `stage` (
                                       `name` ENUM('To Do', 'In Progress', 'Done') NOT NULL,
                                       `final` BIT(1) NOT NULL,
                                       PRIMARY KEY (`name`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `board_stage`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `board_stage` (
                                             `bad_id` BINARY(16) NOT NULL,
                                             `sge_name` ENUM('To Do', 'In Progress', 'Done') NOT NULL,
                                             PRIMARY KEY (`bad_id`, `sge_name`),
                                             CONSTRAINT `bse_sge_fk`
                                                 FOREIGN KEY (`sge_name`)
                                                     REFERENCES `stage` (`name`)
                                                     ON DELETE NO ACTION
                                                     ON UPDATE NO ACTION,
                                             CONSTRAINT `bse_bad_fk`
                                                 FOREIGN KEY (`bad_id`)
                                                     REFERENCES `board` (`mnr_id`)
                                                     ON DELETE NO ACTION
                                                     ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE INDEX `bse_sge_fk_idx` ON `board_stage` (`sge_name` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `student_board`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `student_board` (
                                               `brd_id` BINARY(16) NOT NULL,
                                               `sdt_id` BINARY(16) NOT NULL,
                                               `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               `notification_seen` BIT(1) NOT NULL DEFAULT 0,
                                               CONSTRAINT `sbd_sdt_fk`
                                                   FOREIGN KEY (`sdt_id`)
                                                       REFERENCES `student` (`usr_id`)
                                                       ON DELETE NO ACTION
                                                       ON UPDATE NO ACTION,
                                               CONSTRAINT `sbd_brd_fk`
                                                   FOREIGN KEY (`brd_id`)
                                                       REFERENCES `board` (`mnr_id`)
                                                       ON DELETE NO ACTION
                                                       ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE UNIQUE INDEX `sdt_id_UNIQUE` ON `student_board` (`sdt_id` ASC) VISIBLE;

CREATE UNIQUE INDEX `brd_id_UNIQUE` ON `student_board` (`brd_id` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `task_asignee`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `task_asignee` (
                                              `sbd_brd_id` BINARY(16) NOT NULL,
                                              `sbd_sdt_id` BINARY(16) NOT NULL,
                                              `tsk_id` BINARY(16) NOT NULL,
                                              PRIMARY KEY (`sbd_brd_id`, `sbd_sdt_id`, `tsk_id`),
                                              CONSTRAINT `tae_tsk_fk`
                                                  FOREIGN KEY (`tsk_id`)
                                                      REFERENCES `task` (`id`)
                                                      ON DELETE NO ACTION
                                                      ON UPDATE NO ACTION,
                                              CONSTRAINT `tae_sbd_fk`
                                                  FOREIGN KEY (`sbd_brd_id` , `sbd_sdt_id`)
                                                      REFERENCES `student_board` (`brd_id` , `sdt_id`)
                                                      ON DELETE NO ACTION
                                                      ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE INDEX `tae_tsk_fk_idx` ON `task_asignee` (`tsk_id` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `stage_task`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `stage_task` (
                                            `tsk_id` BINARY(16) NOT NULL,
                                            `bse_sge_name` ENUM('To Do', 'In Progress', 'Done') NOT NULL,
                                            `bse_bad_id` BINARY(16) NOT NULL,
                                            `order` DECIMAL(10,4) NOT NULL,
                                            PRIMARY KEY (`tsk_id`),
                                            CONSTRAINT `stk_tsk_fk`
                                                FOREIGN KEY (`tsk_id`)
                                                    REFERENCES `task` (`id`)
                                                    ON DELETE NO ACTION
                                                    ON UPDATE NO ACTION,
                                            CONSTRAINT `stk_bse_fk`
                                                FOREIGN KEY (`bse_sge_name` , `bse_bad_id`)
                                                    REFERENCES `board_stage` (`sge_name` , `bad_id`)
                                                    ON DELETE NO ACTION
                                                    ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE UNIQUE INDEX `order_UNIQUE` ON `stage_task` (`order` ASC, `bse_sge_name` ASC) VISIBLE;

CREATE INDEX `stk_bse_fk_idx` ON `stage_task` (`bse_sge_name` ASC, `bse_bad_id` ASC) VISIBLE;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `color`
-- -----------------------------------------------------
START TRANSACTION;
USE `kedu_project_manager`;
INSERT INTO `color` (`name`, `code`) VALUES ('Red', 0x23454634343434);
INSERT INTO `color` (`name`, `code`) VALUES ('Orange', 0x23463937333136);
INSERT INTO `color` (`name`, `code`) VALUES ('Amber', 0x23463539453042);
INSERT INTO `color` (`name`, `code`) VALUES ('Yellow', 0x23454142333038);
INSERT INTO `color` (`name`, `code`) VALUES ('Lime', 0x23383443433136);
INSERT INTO `color` (`name`, `code`) VALUES ('Green', 0x23323243353545);
INSERT INTO `color` (`name`, `code`) VALUES ('Emerald', 0x23313042393831);
INSERT INTO `color` (`name`, `code`) VALUES ('Teal', 0x23313442384136);
INSERT INTO `color` (`name`, `code`) VALUES ('Cyan', 0x23303642364434);
INSERT INTO `color` (`name`, `code`) VALUES ('Blue', 0x23334238324636);
INSERT INTO `color` (`name`, `code`) VALUES ('Indigo', 0x23363336364631);
INSERT INTO `color` (`name`, `code`) VALUES ('Violet', 0x23384235434636);
INSERT INTO `color` (`name`, `code`) VALUES ('Fuchsia', 0x23443934364546);
INSERT INTO `color` (`name`, `code`) VALUES ('Pink', 0x23454334383939);
INSERT INTO `color` (`name`, `code`) VALUES ('Rose', 0x23463433463545);
INSERT INTO `color` (`name`, `code`) VALUES ('Gray', 0x23364237323830);
INSERT INTO `color` (`name`, `code`) VALUES ('Slate', 0x23363437343842);
INSERT INTO `color` (`name`, `code`) VALUES ('Zinc', 0x23373137313741);

COMMIT;


-- -----------------------------------------------------
-- Data for table `stage`
-- -----------------------------------------------------
START TRANSACTION;
USE `kedu_project_manager`;
INSERT INTO `stage` (`name`, `final`) VALUES ('To Do', 0);
INSERT INTO `stage` (`name`, `final`) VALUES ('In Progress', 0);
INSERT INTO `stage` (`name`, `final`) VALUES ('Done', 1);

COMMIT;

