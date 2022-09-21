# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

-- init script create procs
-- Inital script to create stored procedures etc for mysql platform
DROP PROCEDURE IF EXISTS usp_ebean_drop_foreign_keys;

delimiter $$
--
-- PROCEDURE: usp_ebean_drop_foreign_keys TABLE, COLUMN
-- deletes all constraints and foreign keys referring to TABLE.COLUMN
--
CREATE PROCEDURE usp_ebean_drop_foreign_keys(IN p_table_name VARCHAR(255), IN p_column_name VARCHAR(255))
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE c_fk_name CHAR(255);
  DECLARE curs CURSOR FOR SELECT CONSTRAINT_NAME from information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE() and TABLE_NAME = p_table_name and COLUMN_NAME = p_column_name
      AND REFERENCED_TABLE_NAME IS NOT NULL;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN curs;

  read_loop: LOOP
    FETCH curs INTO c_fk_name;
    IF done THEN
      LEAVE read_loop;
    END IF;
    SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP FOREIGN KEY ', c_fk_name);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
  END LOOP;

  CLOSE curs;
END
$$

DROP PROCEDURE IF EXISTS usp_ebean_drop_column;

delimiter $$
--
-- PROCEDURE: usp_ebean_drop_column TABLE, COLUMN
-- deletes the column and ensures that all indices and constraints are dropped first
--
CREATE PROCEDURE usp_ebean_drop_column(IN p_table_name VARCHAR(255), IN p_column_name VARCHAR(255))
BEGIN
  CALL usp_ebean_drop_foreign_keys(p_table_name, p_column_name);
  SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
  PREPARE stmt FROM @sql;
  EXECUTE stmt;
END
$$
create table cp_system_action (
  syn_id                        varchar(255) auto_increment not null,
  syn_cname                     varchar(255),
  syn_cdesc                     varchar(255),
  syn_action                    varchar(255),
  syn_name                      varchar(255),
  syn_show                      tinyint(1) default 0 not null,
  syn_sort                      integer not null,
  syn_created                   bigint not null,
  constraint pk_cp_system_action primary key (syn_id)
);

create table v1_activity (
  acy_id                        bigint auto_increment not null,
  acy_name                      varchar(255),
  acy_class                     integer not null,
  acy_total                     decimal(38),
  acy_count_total               decimal(38),
  acy_num                       decimal(38),
  acy_available                 decimal(38),
  acy_count                     integer not null,
  acy_start                     bigint not null,
  acy_end                       bigint not null,
  acy_status                    integer not null,
  acy_max                       decimal(38),
  acy_min                       decimal(38),
  acy_expired                   bigint not null,
  acy_desc                      varchar(255),
  acy_updated                   bigint not null,
  acy_created                   bigint not null,
  constraint pk_v1_activity primary key (acy_id)
);

create table v1_activity_detail (
  acl_id                        bigint auto_increment not null,
  acl_acy_id                    bigint not null,
  acl_acy_name                  varchar(255),
  acl_mer_id                    bigint not null,
  acl_acy_class                 integer not null,
  acl_expired                   bigint not null,
  acl_value                     decimal(38),
  acl_value_available           decimal(38),
  acl_status                    integer not null,
  acl_gettime                   bigint not null,
  acl_updated                   bigint not null,
  acl_created                   bigint not null,
  constraint pk_v1_activity_detail primary key (acl_id)
);

create table cp_member (
  mer_id                        integer auto_increment not null,
  mer_username                  varchar(255),
  mer_realname                  varchar(255),
  mer_password                  varchar(255),
  mer_created                   bigint not null,
  mer_last_time                 bigint not null,
  mer_last_ip                   varchar(255),
  constraint pk_cp_member primary key (mer_id)
);

create table v1_article (
  art_id                        bigint auto_increment not null,
  art_title                     varchar(255),
  art_author                    varchar(255),
  art_source                    varchar(255),
  art_ary_id                    integer not null,
  art_publish                   varchar(255),
  art_status                    integer not null,
  art_top                       integer not null,
  art_recommend                 integer not null,
  art_start                     bigint not null,
  art_end                       bigint not null,
  art_content                   varchar(255),
  art_thumb_url                 varchar(255),
  art_updated                   bigint not null,
  art_created                   bigint not null,
  constraint pk_v1_article primary key (art_id)
);

create table v1_article_category (
  ary_id                        integer auto_increment not null,
  ary_name                      varchar(255),
  ary_order                     integer not null,
  ary_status                    integer not null,
  ary_desc                      varchar(255),
  ary_updated                   bigint not null,
  ary_created                   bigint not null,
  constraint pk_v1_article_category primary key (ary_id)
);

create table v1_balance_log_00 (
  bag_id                        bigint auto_increment not null,
  bag_mer_id                    bigint not null,
  bag_class                     integer not null,
  bag_mer_balance               decimal(38),
  bag_mer_balance_freeze        decimal(38),
  bag_mer_balance_total         decimal(38),
  bag_money                     decimal(38),
  bag_type                      integer not null,
  bag_money_type                integer not null,
  bag_desc                      varchar(255),
  bag_created                   bigint not null,
  constraint pk_v1_balance_log_00 primary key (bag_id)
);

create table v1_balance_person_log_00 (
  bag_id                        bigint auto_increment not null,
  bag_mer_id                    bigint not null,
  bag_class                     integer not null,
  bag_mer_balance               decimal(38),
  bag_mer_balance_freeze        decimal(38),
  bag_mer_balance_total         decimal(38),
  bag_money                     decimal(38),
  bag_type                      integer not null,
  bag_money_type                integer not null,
  bag_desc                      varchar(255),
  bag_created                   bigint not null,
  constraint pk_v1_balance_person_log_00 primary key (bag_id)
);

create table v1_bank (
  bak_id                        bigint auto_increment not null,
  bak_mer_id                    bigint not null,
  bak_default                   integer not null,
  bak_type                      integer not null,
  bak_province                  varchar(255),
  bak_city                      varchar(255),
  bak_name                      varchar(255),
  bak_user                      varchar(255),
  bak_number                    varchar(255),
  bak_updated                   bigint not null,
  bak_created                   bigint not null,
  constraint pk_v1_bank primary key (bak_id)
);

create table v1_batch_trade_log (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  type                          integer not null,
  target_count                  integer not null,
  run_count                     integer not null,
  status                        integer not null,
  base_currency_id              integer not null,
  target_currency_id            integer not null,
  param                         varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_batch_trade_log primary key (id)
);

create table v1_bittrex_currency_order (
  id                            bigint auto_increment not null,
  base_currency_id              integer not null,
  target_currency_id            integer not null,
  order_id                      varchar(255),
  uid                           bigint not null,
  amount                        varchar(255),
  price                         varchar(255),
  total_money                   decimal(38),
  final_amount                  varchar(255),
  final_price                   varchar(255),
  handle_amount                 varchar(255),
  field_amount                  varchar(255),
  field_cash_amount             varchar(255),
  field_fees                    varchar(255),
  platform_fees                 varchar(255),
  source                        varchar(255),
  state                         varchar(255),
  symbol                        varchar(255),
  type                          integer not null,
  platform                      integer not null,
  price_precision               integer not null,
  amount_precision              integer not null,
  handled                       integer not null,
  canceled_at                   bigint not null,
  created_at                    bigint not null,
  pay_fee_by_pt                 tinyint(1) default 0 not null,
  finished_at                   bigint not null,
  constraint pk_v1_bittrex_currency_order primary key (id)
);

create table v1_clear_data_log (
  id                            bigint auto_increment not null,
  content                       varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_clear_data_log primary key (id)
);

create table v1_coin_transfer_log (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  currency_id                   integer not null,
  type                          integer not null,
  sender_address                varchar(255),
  receiver_address              varchar(255),
  transaction                   varchar(255),
  amount                        decimal(38),
  charge_fee                    decimal(38),
  status                        integer not null,
  operator                      integer not null,
  create_time                   bigint not null,
  constraint pk_v1_coin_transfer_log primary key (id)
);

create table v1_commission_white_list (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  account_name                  varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_commission_white_list primary key (id)
);

create table v1_exchange_contact_detail (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  name                          varchar(255),
  province                      varchar(255),
  city                          varchar(255),
  details                       varchar(255),
  postcode                      varchar(255),
  telephone                     varchar(255),
  is_default                    integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_exchange_contact_detail primary key (id)
);

create table v1_crowd_fund_config (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  mode                          integer not null,
  launcher_member_id            bigint not null,
  launcher_member_name          varchar(255),
  currency_id                   integer not null,
  target_amount                 decimal(38),
  start_time                    bigint not null,
  end_time                      bigint not null,
  description                   varchar(255),
  publicity_image               varchar(255),
  status                        integer not null,
  coin_scale                    decimal(38),
  max_unit_per_user             integer not null,
  threshold                     integer not null,
  create_time                   bigint not null,
  update_time                   bigint not null,
  total_like                    bigint not null,
  min_amount                    decimal(38),
  constraint pk_v1_crowd_fund_config primary key (id)
);

create table v1_crowd_fund_level (
  id                            integer auto_increment not null,
  fund_config_id                bigint not null,
  level                         integer not null,
  level_count                   decimal(38),
  description                   varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_crowd_fund_level primary key (id)
);

create table v1_crowd_fund_log (
  id                            bigint auto_increment not null,
  fund_config_id                bigint not null,
  member_id                     bigint not null,
  amount                        decimal(38),
  status                        integer not null,
  create_time                   bigint not null,
  update_time                   bigint not null,
  description                   varchar(255),
  taken_coin_amount             decimal(38),
  left_taken_coin_amount        decimal(38),
  constraint pk_v1_crowd_fund_log primary key (id)
);

create table v1_crowd_fund_project_reward_log (
  id                            bigint auto_increment not null,
  crowd_fund_config_id          bigint not null,
  member_id                     bigint not null,
  currency_id                   integer not null,
  amount                        decimal(38),
  create_time                   bigint not null,
  description                   varchar(255),
  constraint pk_v1_crowd_fund_project_reward_log primary key (id)
);

create table v1_crowd_fund_reward_log (
  id                            bigint auto_increment not null,
  crowd_fund_config_id          bigint not null,
  currency_id                   integer not null,
  amount                        decimal(38),
  create_time                   bigint not null,
  description                   varchar(255),
  status                        integer not null,
  constraint pk_v1_crowd_fund_reward_log primary key (id)
);

create table v1_crowd_fund_schedule (
  id                            bigint auto_increment not null,
  currency_id                   integer not null,
  crowd_fund_config_id          bigint not null,
  status                        integer not null,
  scale                         integer not null,
  coin_scale                    decimal(38),
  update_time                   bigint not null,
  create_time                   bigint not null,
  date                          bigint not null,
  constraint pk_v1_crowd_fund_schedule primary key (id)
);

create table v1_crowd_fund_timeline (
  id                            bigint auto_increment not null,
  crowd_fund_config_id          bigint not null,
  author                        varchar(255),
  content                       varchar(255),
  title                         varchar(255),
  source                        varchar(255),
  status                        integer not null,
  update_time                   bigint not null,
  date                          bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_crowd_fund_timeline primary key (id)
);

create table v1_crowd_fund_unfreeze_log (
  id                            bigint auto_increment not null,
  fund_config_id                bigint not null,
  member_id                     bigint not null,
  currency_id                   integer not null,
  amount                        decimal(38),
  description                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_crowd_fund_unfreeze_log primary key (id)
);

create table v1_currency (
  cuy_id                        integer auto_increment not null,
  cuy_name                      varchar(255),
  cuy_name_en                   varchar(255),
  cuy_abbr                      varchar(255),
  cuy_logo                      varchar(255),
  cuy_offerimage                varchar(255),
  cuy_unit                      varchar(255),
  cuy_status                    integer not null,
  cuy_order                     integer not null,
  cuy_apiurl                    varchar(255),
  wallet_pwd                    varchar(255),
  wallet_type                   integer not null,
  wallet_address                varchar(255),
  wallet_need_sync              integer not null,
  contact_address               varchar(255),
  cuy_desc                      varchar(255),
  cuy_min_withdraw_amount       decimal(38),
  withdraw_fee_ratio            double not null,
  withdraw_fee_amount           double not null,
  allow_to_withdraw             tinyint(1) default 0 not null,
  allow_to_deposit              tinyint(1) default 0 not null,
  need_memo                     tinyint(1) default 0 not null,
  min_confirmation              integer not null,
  start_block_to_scan           bigint not null,
  cuy_updated                   bigint not null,
  cuy_created                   bigint not null,
  constraint pk_v1_currency primary key (cuy_id)
);

create table v1_currency_comment (
  cut_id                        bigint not null,
  currency_cuy_id               integer,
  member_mer_id                 integer,
  cut_score1                    integer not null,
  cut_score2                    integer not null,
  constraint uq_v1_currency_comment_currency_cuy_id unique (currency_cuy_id),
  constraint uq_v1_currency_comment_member_mer_id unique (member_mer_id)
);

create table v1_currency_entrust (
  cut_id                        bigint auto_increment not null,
  cut_mer_id                    bigint not null,
  cut_cur_id                    bigint not null,
  cut_base_cuy_id               integer not null,
  cut_cuy_id                    integer not null,
  cut_order_id                  bigint not null,
  cut_type                      integer not null,
  cut_price                     decimal(38),
  cut_num                       decimal(38),
  cut_total                     decimal(38),
  cut_status                    integer not null,
  cut_deal                      integer not null,
  cut_updated                   bigint not null,
  cut_created                   bigint not null,
  constraint pk_v1_currency_entrust primary key (cut_id)
);

create table v1_currency_exchange_config (
  id                            bigint auto_increment not null,
  source_currency_id            integer not null,
  target_currency_id            integer not null,
  scale                         decimal(38),
  min                           decimal(38),
  total_amount                  decimal(38),
  begin_time                    bigint not null,
  end_time                      bigint not null,
  status                        integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_currency_exchange_config primary key (id)
);

create table v1_currency_kline (
  cue_id                        bigint auto_increment not null,
  cue_base_cuy_id               integer not null,
  cue_cuy_id                    integer not null,
  cue_type                      integer not null,
  cue_num                       decimal(38),
  cue_first                     decimal(38),
  cue_max                       decimal(38),
  cue_min                       decimal(38),
  cue_last                      decimal(38),
  cue_time                      bigint not null,
  cue_created                   bigint not null,
  constraint pk_v1_currency_kline primary key (cue_id)
);

create table v1_currency_link (
  cuk_id                        bigint auto_increment not null,
  cuk_cuy_id                    integer not null,
  cuk_name                      varchar(255),
  cuk_link                      varchar(255),
  cuk_updated                   bigint not null,
  cuk_created                   bigint not null,
  constraint pk_v1_currency_link primary key (cuk_id)
);

create table v1_currency_offer (
  cur_id                        bigint auto_increment not null,
  cur_cuy_id                    integer not null,
  cur_name                      integer not null,
  cur_status                    integer not null,
  cur_start                     bigint not null,
  cur_end                       bigint not null,
  cur_class                     integer not null,
  cur_price                     decimal(38),
  cur_total                     decimal(38),
  cur_show                      decimal(38),
  cur_qualification_num         decimal(38),
  cur_qualification_class       integer not null,
  cur_num                       decimal(38),
  cur_limit_count               integer not null,
  cur_limit_num                 decimal(38),
  cur_updated                   bigint not null,
  cur_created                   bigint not null,
  constraint pk_v1_currency_offer primary key (cur_id)
);

create table v1_currency_order (
  id                            bigint auto_increment not null,
  base_currency_id              integer not null,
  target_currency_id            integer not null,
  order_id                      varchar(255),
  uid                           bigint not null,
  amount                        varchar(255),
  price                         varchar(255),
  total_money                   decimal(38),
  final_amount                  varchar(255),
  final_price                   varchar(255),
  handle_amount                 varchar(255),
  field_amount                  varchar(255),
  field_cash_amount             varchar(255),
  field_fees                    varchar(255),
  platform_fees                 varchar(255),
  source                        varchar(255),
  state                         varchar(255),
  symbol                        varchar(255),
  type                          integer not null,
  platform                      integer not null,
  price_precision               integer not null,
  amount_precision              integer not null,
  handled                       integer not null,
  canceled_at                   bigint not null,
  created_at                    bigint not null,
  pay_fee_by_pt                 tinyint(1) default 0 not null,
  finished_at                   bigint not null,
  constraint pk_v1_currency_order primary key (id)
);

create table v1_currency_vote (
  cuy_id                        bigint auto_increment not null,
  cuy_name                      varchar(255),
  cuy_name_en                   varchar(255),
  cuy_abbr                      varchar(255),
  cuy_price                     decimal(38),
  cuy_done                      decimal(38),
  cuy_logo                      varchar(255),
  cuy_offerimage                varchar(255),
  cuy_unit                      varchar(255),
  cuy_status                    integer not null,
  cuy_order                     integer not null,
  cuy_commission                decimal(38),
  cuy_apiurl                    varchar(255),
  cuy_runpid                    varchar(255),
  cuy_ownnum                    varchar(255),
  cuy_website                   varchar(255),
  cuy_owner                     varchar(255),
  cuy_algorithmic               varchar(255),
  cuy_publish_time              varchar(255),
  cuy_speed                     varchar(255),
  cuy_total                     varchar(255),
  cuy_leftnum                   varchar(255),
  cuy_prove                     varchar(255),
  cuy_difficulty                varchar(255),
  cuy_gift                      varchar(255),
  cuy_specialty                 varchar(255),
  cuy_deficiency                varchar(255),
  cuy_desc                      varchar(255),
  cuy_updated                   bigint not null,
  cuy_created                   bigint not null,
  constraint pk_v1_currency_vote primary key (cuy_id)
);

create table v1_deposit (
  det_id                        bigint auto_increment not null,
  det_money                     decimal(38),
  det_realmoney                 decimal(38),
  det_bank_name                 varchar(255),
  det_bank_number               varchar(255),
  det_bank_user                 varchar(255),
  det_mer_id                    bigint not null,
  det_sbank_name                varchar(255),
  det_sbank_number              varchar(255),
  det_sbank_user                varchar(255),
  det_sbank_phone               varchar(255),
  det_desc                      varchar(255),
  det_status                    integer not null,
  det_updated                   bigint not null,
  det_created                   bigint not null,
  constraint pk_v1_deposit primary key (det_id)
);

create table v1_etc_in_log (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  currency_id                   integer not null,
  status                        integer not null,
  tx_block_no                   bigint not null,
  amount                        decimal(38),
  tx_hash                       varchar(255),
  tx_hash_2                     varchar(255),
  from_address                  varchar(255),
  to_address                    varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_etc_in_log primary key (id)
);

create table v1_gz_config (
  id                            integer auto_increment not null,
  name                          varchar(255),
  from_currency_id              integer not null,
  min_amount                    decimal(38),
  max_amount                    decimal(38),
  sold_amount                   decimal(38),
  total_amount                  decimal(38),
  project_id                    integer not null,
  to_currency_id                integer not null,
  ratio                         double not null,
  invitor_award_ratio           double not null,
  invitor_award_unlock_quora_ratio double not null,
  start_time                    bigint not null,
  end_time                      bigint not null,
  enable                        tinyint(1) default 0 not null,
  note                          varchar(255),
  whilte_list                   varchar(255),
  sort                          integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  from_currency_abbr            varchar(255),
  to_currency_abbr              varchar(255),
  constraint pk_v1_gz_config primary key (id)
);

create table v1_gz_invite_award_log (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  master_uid                    bigint not null,
  buy_amount                    decimal(38),
  award_amount                  decimal(38),
  from_currency_abbr            varchar(255),
  to_currency_abbr              varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_gz_invite_award_log primary key (id)
);

create table v1_gz_invite_log (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  master_id                     bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_gz_invite_log primary key (id)
);

create table v1_gz_log (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  config_id                     bigint not null,
  config_name                   varchar(255),
  from_currency_abbr            varchar(255),
  to_currency_abbr              varchar(255),
  from_amount                   decimal(38),
  to_amount                     decimal(38),
  ratio                         double not null,
  note                          varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_gz_log primary key (id)
);

create table v1_gz_project (
  id                            integer auto_increment not null,
  name                          varchar(255),
  from_currency_sold_amount     decimal(38),
  to_currency_sold_amount       decimal(38),
  from_currency_abbr            varchar(255),
  from_currency_id              integer not null,
  to_currency_id                integer not null,
  to_currency_abbr              varchar(255),
  note                          varchar(255),
  brief_note                    varchar(255),
  thumb_img_url                 varchar(255),
  from_currency_max_amount      double not null,
  status                        integer not null,
  white_list                    varchar(255),
  sort                          integer not null,
  begin_time                    bigint not null,
  end_time                      bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_gz_project primary key (id)
);

create table v1_gz_unlock_quora (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  enable                        tinyint(1) default 0 not null,
  unlock_quora_by_invite        decimal(38),
  unlock_quora_every_day        decimal(38),
  lock_amount                   decimal(38),
  create_time                   bigint not null,
  constraint pk_v1_gz_unlock_quora primary key (id)
);

create table v1_gz_stat (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  config_id                     integer not null,
  config_name                   varchar(255),
  source_total_amount           decimal(38),
  target_total_amount           decimal(38),
  create_time                   bigint not null,
  constraint pk_v1_gz_stat primary key (id)
);

create table v1_gz_invite_total (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  total_amount                  decimal(38),
  award_amount                  decimal(38),
  from_currency_abbr            varchar(255),
  to_currency_abbr              varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_gz_invite_total primary key (id)
);

create table v1_gz_stat_total (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  total_amount                  decimal(38),
  target_total_amount           decimal(38),
  create_time                   bigint not null,
  constraint pk_v1_gz_stat_total primary key (id)
);

create table v1_upgrade_grey_user (
  id                            bigint auto_increment not null,
  user_name                     varchar(255),
  uid                           bigint not null,
  note                          varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_upgrade_grey_user primary key (id)
);

create table cp_group (
  grp_id                        integer auto_increment not null,
  grp_name                      varchar(255),
  grp_remark                    varchar(255),
  grp_created                   bigint not null,
  constraint pk_cp_group primary key (grp_id)
);

create table cp_group_action (
  grn_id                        integer auto_increment not null,
  grn_grp_id                    integer not null,
  grn_syn_id                    varchar(255),
  constraint pk_cp_group_action primary key (grn_id)
);

create table cp_group_user (
  grr_id                        integer auto_increment not null,
  grr_grp_id                    integer not null,
  grr_grp_name                  varchar(255),
  grr_mer_id                    integer not null,
  grr_mer_realname              varchar(255),
  constraint pk_cp_group_user primary key (grr_id)
);

create table v1_interest (
  id                            bigint auto_increment not null,
  currency_id                   integer not null,
  interest                      decimal(38),
  status                        integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_interest primary key (id)
);

create table v1_interest_log (
  id                            bigint auto_increment not null,
  currency_id                   integer not null,
  member_id                     bigint not null,
  interest                      decimal(38),
  amount                        decimal(38),
  claim_time                    varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_interest_log primary key (id)
);

create table v1_statistics_interest (
  id                            bigint auto_increment not null,
  currency_id                   integer not null,
  claim_amount                  decimal(38),
  claim_count                   integer not null,
  claim_date                    varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_statistics_interest primary key (id)
);

create table cp_log (
  log_id                        bigint auto_increment not null,
  log_unique                    varchar(255),
  log_mer_id                    integer not null,
  log_param                     varchar(255),
  log_created                   bigint not null,
  constraint pk_cp_log primary key (log_id)
);

create table v1_login_log_00 (
  log_id                        bigint auto_increment not null,
  log_mer_id                    bigint not null,
  log_ip                        varchar(255),
  log_place                     varchar(255),
  log_created                   bigint not null,
  constraint pk_v1_login_log_00 primary key (log_id)
);

create table v1_lucky (
  id                            bigint auto_increment not null,
  prize_class                   integer not null,
  prize_class_name              varchar(255),
  chance                        integer not null,
  inner_min_angel               integer not null,
  inner_max_angel               integer not null,
  outer_min_angel               integer not null,
  outer_max_angel               integer not null,
  amount                        integer not null,
  prize_currency_id             integer not null,
  prize_currency_amount         decimal(38),
  create_time                   bigint not null,
  update_time                   bigint not null,
  constraint pk_v1_lucky primary key (id)
);

create table v1_lucky_config (
  id                            bigint auto_increment not null,
  enable                        integer not null,
  currency_id                   integer not null,
  max_per_day                   integer not null,
  consume_amount                decimal(38),
  description                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_lucky_config primary key (id)
);

create table v1_lucky_log (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  lucky_id                      bigint not null,
  address_id                    bigint not null,
  create_time                   bigint not null,
  status                        integer not null,
  constraint pk_v1_lucky_log primary key (id)
);

create table v1_member (
  mer_id                        integer auto_increment not null,
  mer_email                     varchar(256) not null,
  mer_password                  varchar(255),
  mer_password_pay              varchar(255),
  mer_status                    integer not null,
  mer_confirmation_email        integer not null,
  mer_notice_login              integer not null,
  mer_notice_withdraw           integer not null,
  mer_trade_set                 integer not null,
  auth_level                    integer not null,
  auth_time                     bigint not null,
  mer_confirmation_phone        integer not null,
  mer_auth_type                 integer not null,
  mer_need_two_auth             integer not null,
  mer_auth_key                  varchar(255),
  mer_auth_num                  varchar(255),
  mer_front_img                 varchar(255),
  mer_back_img                  varchar(255),
  mer_hand_hold_img             varchar(255),
  mer_realname                  varchar(255),
  mer_phone                     varchar(255),
  mer_mail                      varchar(255),
  mer_lastip                    varchar(255),
  mer_lasttime                  bigint not null,
  mer_logincount                integer not null,
  mer_last_login_place          varchar(255),
  mer_desc                      varchar(255),
  mer_auth_apply_approve_note   varchar(255),
  mer_updated                   bigint not null,
  mer_created                   bigint not null,
  mer_score                     bigint not null,
  mer_level                     integer not null,
  mer_is_oversea_user           tinyint(1) default 0 not null,
  constraint uq_v1_member_mer_email unique (mer_email),
  constraint pk_v1_member primary key (mer_id)
);

create table v1_member_balance (
  mee_id                        bigint auto_increment not null,
  mee_mer_id                    bigint not null,
  mee_class                     integer not null,
  mee_address                   varchar(255),
  mee_wallet_file_name          varchar(255),
  mee_balance                   decimal(38),
  mee_balance_freeze            decimal(38),
  mee_total                     decimal(38),
  mee_updated                   bigint not null,
  mee_created                   bigint not null,
  mee_public_key                varchar(255),
  mee_pass_phase                varchar(255),
  mee_wallet_account_id         varchar(255),
  constraint pk_v1_member_balance primary key (mee_id)
);

create table v1_member_balance_old (
  mee_id                        bigint auto_increment not null,
  mee_mer_id                    bigint not null,
  mee_class                     integer not null,
  mee_address                   varchar(255),
  mee_balance                   decimal(38),
  mee_balance_freeze            decimal(38),
  mee_total                     decimal(38),
  mee_updated                   bigint not null,
  mee_created                   bigint not null,
  mee_public_key                varchar(255),
  mee_pass_phase                varchar(255),
  mee_wallet_account_id         varchar(255),
  constraint pk_v1_member_balance_old primary key (mee_id)
);

create table v1_member_level (
  id                            integer auto_increment not null,
  need_score                    bigint not null,
  level                         integer not null,
  level_name                    varchar(255),
  rmb_withdraw_charge_fee       decimal(38),
  coin_withdraw_charge_fee      decimal(38),
  transaction_buy_fee           decimal(38),
  transaction_sell_fee          decimal(38),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_member_level primary key (id)
);

create table v1_member_like (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  type                          integer not null,
  target_id                     bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_member_like primary key (id)
);

create table v1_member_old (
  mer_id                        integer auto_increment not null,
  mer_email                     varchar(256) not null,
  mer_password                  varchar(255),
  mer_password_pay              varchar(255),
  mer_status                    integer not null,
  mer_confirmation_email        integer not null,
  mer_notice_login              integer not null,
  mer_notice_withdraw           integer not null,
  mer_trade_set                 integer not null,
  mer_confirmation_person       integer not null,
  mer_confirmation_phone        integer not null,
  mer_auth_type                 integer not null,
  mer_auth_num                  varchar(255),
  mer_auth_img                  varchar(255),
  mer_realname                  varchar(255),
  mer_phone                     varchar(255),
  mer_lastip                    varchar(255),
  mer_lasttime                  bigint not null,
  mer_logincount                integer not null,
  mer_last_login_place          varchar(255),
  mer_desc                      varchar(255),
  mer_updated                   bigint not null,
  mer_created                   bigint not null,
  mer_score                     bigint not null,
  mer_level                     integer not null,
  constraint uq_v1_member_old_mer_email unique (mer_email),
  constraint pk_v1_member_old primary key (mer_id)
);

create table v1_member_score_config (
  id                            bigint auto_increment not null,
  type                          integer not null,
  description                   varchar(255),
  score                         bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_member_score_config primary key (id)
);

create table v1_member_score_log (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  score                         bigint not null,
  type                          integer not null,
  reason_type                   integer not null,
  description                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_member_score_log primary key (id)
);

create table v1_two_auth_config (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  login_need                    tinyint(1) default 0 not null,
  modify_withdraw_address_need  tinyint(1) default 0 not null,
  modify_transaction_password_need tinyint(1) default 0 not null,
  create_api_need               tinyint(1) default 0 not null,
  constraint pk_v1_two_auth_config primary key (id)
);

create table v1_exchange_merchants (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  category_id                   bigint not null,
  img_url                       varchar(255),
  stock                         bigint not null,
  details                       varchar(255),
  attributes                    varchar(255),
  status                        integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  market_price                  decimal(38),
  type                          integer not null,
  issue                         varchar(255),
  constraint pk_v1_exchange_merchants primary key (id)
);

create table v1_exchange_category (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  is_shown                      integer not null,
  display_order                 integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_exchange_category primary key (id)
);

create table v1_otc_ad (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  nick_name                     varchar(255),
  deal_type                     bigint not null,
  base_currency_id              integer not null,
  status                        integer not null,
  pay_methods                   varchar(255),
  note                          varchar(255),
  price                         double not null,
  able_deal_amount              decimal(38),
  min_trade_value               decimal(38),
  max_trade_value               decimal(38),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_otc_ad primary key (id)
);

create table v1_otc_coin (
  id                            integer auto_increment not null,
  base_currency_id              integer not null,
  base_currency_abbr            varchar(255),
  icon                          varchar(255),
  status                        integer not null,
  sort                          integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_otc_coin primary key (id)
);

create table v1_otc_complaint (
  id                            bigint auto_increment not null,
  otc_order_id                  bigint not null,
  complain_uid                  bigint not null,
  target_uid                    bigint not null,
  note                          varchar(255),
  attachment1                   varchar(255),
  attachment2                   varchar(255),
  attachment3                   varchar(255),
  attachment4                   varchar(255),
  attachment5                   varchar(255),
  status                        integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_otc_complaint primary key (id)
);

create table v1_otc_dealer (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  dealer_name                   varchar(255),
  credit_score                  integer not null,
  audit_status                  integer not null,
  note                          varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_otc_dealer primary key (id)
);

create table v1_otc_order (
  id                            bigint auto_increment not null,
  base_currency_id              integer not null,
  deal_type                     integer not null,
  ad_id                         bigint not null,
  starter_id                    bigint not null,
  starter_nick_name             varchar(255),
  joiner_id                     bigint not null,
  note                          varchar(255),
  mark_code                     varchar(255),
  price                         double not null,
  deal_amount                   decimal(38),
  total_money                   double not null,
  status                        integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_otc_order primary key (id)
);

create table v1_offer (
  ofr_id                        bigint auto_increment not null,
  ofr_cur_id                    bigint not null,
  ofr_cuy_id                    integer not null,
  ofr_mer_id                    bigint not null,
  ofr_class                     integer not null,
  ofr_price                     decimal(38),
  ofr_num                       decimal(38),
  ofr_created                   bigint not null,
  constraint pk_v1_offer primary key (ofr_id)
);

create table v1_exchange_order (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  price_id                      bigint not null,
  currency_id                   integer not null,
  address_id                    bigint not null,
  amount                        bigint not null,
  total_money                   decimal(38),
  status                        integer not null,
  logistics_id                  varchar(255),
  attributes                    varchar(255),
  description                   varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_exchange_order primary key (id)
);

create table v1_otc_black_list (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  account_name                  varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_otc_black_list primary key (id)
);

create table v1_overview (
  stw_id                        bigint auto_increment not null,
  stw_date                      date,
  stw_class                     integer not null,
  stw_balance                   decimal(38),
  stw_charge                    decimal(38),
  stw_promot                    decimal(38),
  stw_withdraw                  decimal(38),
  stw_withdraw_scale            integer not null,
  stw_total                     decimal(38),
  stw_scale                     integer not null,
  stw_result                    decimal(38),
  stw_created                   bigint not null,
  constraint pk_v1_overview primary key (stw_id)
);

create table v1_partner (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  name                          varchar(255),
  invites                       integer not null,
  invite_code                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_partner primary key (id)
);

create table v1_partner_currency (
  id                            integer auto_increment not null,
  currency_id                   integer not null,
  abbr                          varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_partner_currency primary key (id)
);

create table v1_partner_promotion (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  name                          varchar(255),
  master_uid                    bigint not null,
  master_name                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_partner_promotion primary key (id)
);

create table v1_partner_promotion_trade (
  id                            bigint auto_increment not null,
  base_currency_id              integer not null,
  target_currency_id            integer not null,
  trade_type                    integer not null,
  uid                           bigint not null,
  master_uid                    bigint not null,
  price                         decimal(38),
  amount                        decimal(38),
  create_time                   bigint not null,
  constraint pk_v1_partner_promotion_trade primary key (id)
);

create table v1_partner_stat (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  name                          varchar(255),
  date                          varchar(255),
  amount                        decimal(38),
  price                         decimal(38),
  currency_id                   integer not null,
  note                          varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_partner_stat primary key (id)
);

create table v1_partner_sub (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  master_id                     bigint not null,
  name                          varchar(255),
  invites                       integer not null,
  invite_code                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_partner_sub primary key (id)
);

create table v1_pay_log (
  pag_id                        bigint auto_increment not null,
  pag_mer_id                    bigint not null,
  pag_money                     decimal(38),
  pag_bank                      varchar(255),
  pag_products                  varchar(255),
  pag_status                    integer not null,
  pag_updated                   bigint not null,
  pag_created                   bigint not null,
  constraint pk_v1_pay_log primary key (pag_id)
);

create table v1_point_card (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  left_amount                   double not null,
  freeze_amount                 double not null,
  total_amount                  double not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_point_card primary key (id)
);

create table v1_point_card_config (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  need_currency_id              integer not null,
  need_amount                   double not null,
  total_count                   integer not null,
  sold_count                    integer not null,
  give_currency_id              integer not null,
  give_amount                   double not null,
  start_time                    bigint not null,
  end_time                      bigint not null,
  enable                        tinyint(1) default 0 not null,
  note                          varchar(255),
  sort                          integer not null,
  point_card_amount             integer not null,
  min_amount                    integer not null,
  max_amount                    integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_point_card_config primary key (id)
);

create table v1_point_card_log (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  config_id                     bigint not null,
  config_name                   varchar(255),
  left_amount                   double not null,
  freeze_amount                 double not null,
  total_amount                  double not null,
  change_amount                 double not null,
  price                         double not null,
  log_type                      integer not null,
  share                         integer not null,
  note                          varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_point_card_log primary key (id)
);

create table v1_point_card_transfer_log (
  id                            bigint auto_increment not null,
  from_uid                      bigint not null,
  from_account_name             varchar(255),
  to_uid                        bigint not null,
  to_account_name               varchar(255),
  amount                        double not null,
  price                         double not null,
  total_money                   double not null,
  status                        integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_point_card_transfer_log primary key (id)
);

create table v1_exchange_price (
  id                            bigint auto_increment not null,
  merchant_id                   bigint not null,
  currency_id                   integer not null,
  old_price                     decimal(38),
  current_price                 decimal(38),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_exchange_price primary key (id)
);

create table v1_promot (
  prt_id                        bigint auto_increment not null,
  prt_mer_id                    bigint not null,
  prt_invite                    integer not null,
  prt_money                     decimal(38),
  prt_status                    integer not null,
  prt_icode                     varchar(255),
  prt_updated                   bigint not null,
  prt_created                   bigint not null,
  constraint pk_v1_promot primary key (prt_id)
);

create table v1_promot_log (
  prg_id                        integer auto_increment not null,
  prg_prt_id                    bigint not null,
  prg_cut_id                    bigint not null,
  prg_mer_id                    bigint not null,
  prg_source_id                 bigint not null,
  prg_class                     integer not null,
  prg_total                     decimal(38),
  prg_charge                    decimal(38),
  prg_scale                     decimal(38),
  prg_money                     decimal(38),
  prg_created                   bigint not null,
  constraint pk_v1_promot_log primary key (prg_id)
);

create table v1_promot_member (
  prr_id                        bigint auto_increment not null,
  prr_prt_id                    bigint not null,
  prr_mer_id                    bigint not null,
  prr_master                    bigint not null,
  prr_isaward                   integer not null,
  prr_has_charged               tinyint(1) default 0 not null,
  prr_created                   bigint not null,
  constraint pk_v1_promot_member primary key (prr_id)
);

create table v1_member_ready_balance (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  currency_id                   integer not null,
  handled                       tinyint(1) default 0 not null,
  note                          varchar(255),
  left_balance_to_change        decimal(38),
  freeze_balance_to_change      decimal(38),
  total_balance_to_change       decimal(38),
  change_amount                 decimal(38),
  create_time                   bigint not null,
  constraint pk_v1_member_ready_balance primary key (id)
);

create table v1_reg_activity (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  currency_id                   integer not null,
  start_time                    bigint not null,
  end_time                      bigint not null,
  status                        integer not null,
  description                   varchar(255),
  update_time                   bigint not null,
  created_time                  bigint not null,
  award_to_reg_user             decimal(38),
  award_to_invite_user          decimal(38),
  constraint pk_v1_reg_activity primary key (id)
);

create table v1_reg_activity_log (
  id                            bigint auto_increment not null,
  reg_user_id                   bigint not null,
  invite_user_id                bigint not null,
  invite_code                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_reg_activity_log primary key (id)
);

create table v1_remittance (
  ree_id                        bigint auto_increment not null,
  ree_money                     decimal(38),
  ree_bank                      varchar(255),
  ree_name                      varchar(255),
  ree_number                    varchar(255),
  ree_remark                    varchar(255),
  ree_status                    integer not null,
  ree_updated                   bigint not null,
  ree_created                   bigint not null,
  constraint pk_v1_remittance primary key (ree_id)
);

create table v1_reward (
  stg_id                        bigint auto_increment not null,
  stg_mer_id                    bigint not null,
  stg_date                      date,
  stg_class                     integer not null,
  stg_balance                   decimal(38),
  stg_balance_total             decimal(38),
  stg_scale                     decimal(38),
  stg_reward                    decimal(38),
  stg_money                     decimal(38),
  stg_status                    integer not null,
  stg_updated                   bigint not null,
  stg_created                   bigint not null,
  constraint pk_v1_reward primary key (stg_id)
);

create table v1_robot_config (
  id                            bigint auto_increment not null,
  account_id                    bigint not null,
  account_name                  varchar(255),
  login_password                varchar(255),
  pay_password                  varchar(255),
  down_range                    integer not null,
  up_range                      integer not null,
  monitor_range                 integer not null,
  enable                        tinyint(1) default 0 not null,
  base_currency_id              integer not null,
  target_currency_id            integer not null,
  buy_duration                  bigint not null,
  buy_self_own_after            bigint not null,
  base_price                    decimal(38),
  max_buy_counts_one_day        integer not null,
  create_time                   bigint not null,
  token                         varchar(255),
  constraint pk_v1_robot_config primary key (id)
);

create table v1_robot_log (
  id                            bigint auto_increment not null,
  account_id                    bigint not null,
  base_currency_id              integer not null,
  target_currency_id            integer not null,
  type                          integer not null,
  amount                        decimal(38),
  price                         decimal(38),
  create_time                   bigint not null,
  constraint pk_v1_robot_log primary key (id)
);

create table v1_robot_user (
  id                            integer auto_increment not null,
  member_id                     bigint not null,
  user_name                     varchar(255),
  status                        integer not null,
  base_currency_id              integer not null,
  target_currency_id            integer not null,
  divider                       integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_robot_user primary key (id)
);

create table v1_sms_log (
  id                            bigint auto_increment not null,
  sent                          tinyint(1) default 0 not null,
  phone_number                  varchar(255),
  msg_id                        varchar(255),
  content                       varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_sms_log primary key (id)
);

create table v1_statistics_balance (
  id                            bigint auto_increment not null,
  currency_id                   integer not null,
  total                         decimal(38),
  create_time                   bigint not null,
  constraint pk_v1_statistics_balance primary key (id)
);

create table v1_statistics_balance_log_00 (
  stg_id                        integer auto_increment not null,
  stg_mer_id                    bigint not null,
  stg_date                      varchar(255),
  stg_class                     integer not null,
  stg_balance                   decimal(38),
  stg_balance_total             decimal(38),
  stg_scale                     decimal(38),
  stg_reward                    decimal(38),
  stg_money                     decimal(38),
  stg_status                    integer not null,
  stg_updated                   bigint not null,
  stg_created                   bigint not null,
  constraint pk_v1_statistics_balance_log_00 primary key (stg_id)
);

create table v1_statistics_money (
  sty_id                        integer auto_increment not null,
  sty_class                     integer not null,
  sty_in                        decimal(38),
  sty_out                       decimal(38),
  sty_date                      varchar(255),
  sty_created                   bigint not null,
  constraint pk_v1_statistics_money primary key (sty_id)
);

create table v1_statistics_overview (
  stw_id                        bigint auto_increment not null,
  stw_date                      varchar(255),
  stw_class                     integer not null,
  stw_balance                   decimal(38),
  stw_charge                    decimal(38),
  stw_promot                    decimal(38),
  stw_withdraw                  decimal(38),
  stw_withdraw_scale            integer not null,
  stw_total                     decimal(38),
  stw_scale                     integer not null,
  stw_result                    decimal(38),
  stw_created                   bigint not null,
  constraint pk_v1_statistics_overview primary key (stw_id)
);

create table v1_statistics_reg (
  stg_id                        bigint auto_increment not null,
  stg_total                     integer not null,
  stg_date                      varchar(255),
  stg_created                   bigint not null,
  constraint pk_v1_statistics_reg primary key (stg_id)
);

create table v1_statistics_vdeal (
  id                            bigint auto_increment not null,
  date                          varchar(255),
  currency_id                   integer not null,
  win_total                     decimal(38),
  win_count                     bigint not null,
  lose_total                    decimal(38),
  lose_count                    bigint not null,
  even_count                    bigint not null,
  win_members_count             bigint not null,
  lose_members_count            bigint not null,
  join_members                  bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_statistics_vdeal primary key (id)
);

create table v1_system_carousel (
  syl_id                        integer auto_increment not null,
  syl_name                      varchar(255),
  syl_img                       varchar(255),
  syl_url                       varchar(255),
  syl_order                     integer not null,
  syl_desc                      varchar(255),
  mobile_img                    varchar(255),
  mobile_url                    varchar(255),
  syl_updated                   bigint not null,
  syl_created                   bigint not null,
  constraint pk_v1_system_carousel primary key (syl_id)
);

create table v1_system (
  sys_id                        integer auto_increment not null,
  sys_key                       varchar(255),
  sys_value                     varchar(255),
  sys_updated                   bigint not null,
  constraint pk_v1_system primary key (sys_id)
);

create table v1_system_link (
  syk_id                        integer auto_increment not null,
  syk_name                      varchar(255),
  syk_url                       varchar(255),
  syk_order                     integer not null,
  syk_status                    integer not null,
  syk_desc                      varchar(255),
  syk_updated                   bigint not null,
  syk_created                   bigint not null,
  constraint pk_v1_system_link primary key (syk_id)
);

create table v1_task_award (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  currency_id                   integer not null,
  log_id                        bigint not null,
  task_id                       bigint not null,
  amount                        decimal(38),
  status                        integer not null,
  task_name                     varchar(255),
  available_time                bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_task_award primary key (id)
);

create table v1_task_config (
  id                            bigint auto_increment not null,
  task_name                     varchar(255),
  begin_time                    bigint not null,
  end_time                      bigint not null,
  status                        integer not null,
  task_type                     integer not null,
  task_desc                     varchar(255),
  img_url                       varchar(255),
  award_desc                    varchar(255),
  award_type                    integer not null,
  task_limit_day                integer not null,
  demand_currency_id            integer not null,
  demand_currency_amount        decimal(38),
  currency_id                   integer not null,
  max_join_times                integer not null,
  max_join_members              bigint not null,
  rule_json                     varchar(255),
  target_amount                 decimal(38),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_task_config primary key (id)
);

create table v1_task_log (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  master_id                     bigint not null,
  task_id                       bigint not null,
  progress                      integer not null,
  task_type                     integer not null,
  status                        integer not null,
  task_name                     varchar(255),
  task_amount                   decimal(38),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_task_log primary key (id)
);

create table v1_currency_kline_3rd_party (
  cue_id                        bigint auto_increment not null,
  cue_base_cuy_id               integer not null,
  cue_cuy_id                    integer not null,
  cue_type                      integer not null,
  cue_num                       decimal(38),
  cue_first                     decimal(38),
  cue_max                       decimal(38),
  cue_min                       decimal(38),
  cue_last                      decimal(38),
  cue_time                      bigint not null,
  cue_created                   bigint not null,
  constraint pk_v1_currency_kline_3rd_party primary key (cue_id)
);

create table v1_trade (
  tre_id                        bigint auto_increment not null,
  tre_base_cuy_id               integer not null,
  tre_cuy_id                    integer not null,
  tre_cut_id                    bigint not null,
  tre_cut_type                  integer not null,
  tre_source                    integer not null,
  tre_mer_id                    bigint not null,
  tre_price                     decimal(38),
  tre_num                       decimal(38),
  tre_commission                decimal(38),
  tre_total_rmb                 decimal(38),
  pay_fee_by_pt                 tinyint(1) default 0 not null,
  tre_created                   bigint not null,
  constraint pk_v1_trade primary key (tre_id)
);

create table v1_trade_pair (
  id                            integer auto_increment not null,
  base_currency_id              integer not null,
  base_currency_abbr            varchar(255),
  status                        integer not null,
  sort                          integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_trade_pair primary key (id)
);

create table v1_trade_zone (
  id                            integer auto_increment not null,
  base_currency_id              integer not null,
  base_currency_name            varchar(255),
  target_currency_id            integer not null,
  target_currency_name          varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  status                        integer not null,
  min_trade_amount              double not null,
  sell_commission               double not null,
  buy_commission                double not null,
  num_bit                       integer not null,
  price_bit                     integer not null,
  type                          integer not null,
  display_order                 integer not null,
  open_time                     integer not null,
  close_time                    integer not null,
  max_down                      integer not null,
  max_up                        integer not null,
  is_recommend                  tinyint(1) default 0 not null,
  sort                          integer not null,
  platform                      integer not null,
  need_cancel_entrust           tinyint(1) default 0 not null,
  constraint pk_v1_trade_zone primary key (id)
);

create table v1_currency_log (
  cug_id                        bigint auto_increment not null,
  cug_mer_id                    bigint not null,
  cug_cuy_id                    integer not null,
  cug_type                      integer not null,
  cug_sendaddr                  varchar(255),
  cug_receiveaddr               varchar(255),
  cug_intix                     varchar(255),
  cug_confirm                   integer not null,
  cug_num                       decimal(38),
  cug_realnum                   decimal(38),
  cug_charge                    decimal(38),
  cug_dealtime                  bigint not null,
  cug_status                    integer not null,
  cug_status_desc               varchar(255),
  cug_updated                   bigint not null,
  cug_created                   bigint not null,
  constraint pk_v1_currency_log primary key (cug_id)
);

create table v1_currency_outlog (
  cug_id                        bigint auto_increment not null,
  cug_mer_id                    bigint not null,
  cug_cuy_id                    integer not null,
  cug_type                      integer not null,
  cug_sendaddr                  varchar(255),
  cug_receiveaddr               varchar(255),
  cug_intix                     varchar(255),
  tag                           varchar(255),
  cug_confirm                   integer not null,
  cug_num                       decimal(38),
  cug_realnum                   decimal(38),
  cug_charge                    decimal(38),
  cug_dealtime                  bigint not null,
  cug_status                    integer not null,
  cug_status_desc               varchar(255),
  cug_updated                   bigint not null,
  cug_created                   bigint not null,
  constraint pk_v1_currency_outlog primary key (cug_id)
);

create table v1_exchange_treasure_log (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  price_id                      bigint not null,
  number                        varchar(255),
  issue                         varchar(255),
  open_code                     varchar(255),
  total                         bigint not null,
  status                        integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_exchange_treasure_log primary key (id)
);

create table v1_exchange_treasure_number (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  price_id                      bigint not null,
  number                        varchar(255),
  update_time                   bigint not null,
  logistics                     varchar(255),
  status                        integer not null,
  address_id                    bigint not null,
  description                   varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_exchange_treasure_number primary key (id)
);

create table v1_upgrade_config (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  version_number                integer not null,
  enable                        tinyint(1) default 0 not null,
  force_update_rule             varchar(255),
  normal_update_rule            varchar(255),
  update_url                    varchar(255),
  version_type                  integer not null,
  platform                      varchar(255),
  note                          varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_upgrade_config primary key (id)
);

create table v1_vdeal_black_list (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  account_name                  varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_vdeal_black_list primary key (id)
);

create table v1_vdeal_config (
  id                            bigint auto_increment not null,
  status                        integer not null,
  limit_count                   integer not null,
  limit_interval                integer not null,
  open_time                     integer not null,
  close_time                    integer not null,
  max_unknown_allow             integer not null,
  constraint pk_v1_vdeal_config primary key (id)
);

create table v1_vdeal_currency_config (
  id                            integer auto_increment not null,
  currency_id                   integer not null,
  display_order                 integer not null,
  status                        integer not null,
  dealer_id                     bigint not null,
  max_amount                    decimal(38),
  min_amount                    decimal(38),
  constraint pk_v1_vdeal_currency_config primary key (id)
);

create table v1_vdeal_config_interval (
  id                            integer auto_increment not null,
  interval_type_name            varchar(255),
  display_order                 integer not null,
  charge_fee_scale              integer not null,
  interval_minutes              integer not null,
  constraint pk_v1_vdeal_config_interval primary key (id)
);

create table v1_vdeal_log (
  id                            bigint auto_increment not null,
  member_id                     bigint not null,
  interval_type                 integer not null,
  interval_type_name            varchar(255),
  deal_type                     integer not null,
  deal_amount                   decimal(38),
  result_amount                 decimal(38),
  currency_id                   integer not null,
  status                        integer not null,
  begin_price                   decimal(38),
  begin_price_time              bigint not null,
  end_price                     decimal(38),
  end_price_time                bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_vdeal_log primary key (id)
);

create table v1_vdeal_third_party (
  id                            integer auto_increment not null,
  name                          varchar(255),
  code                          varchar(255),
  rest_api_url                  varchar(255),
  socket_api_url                varchar(255),
  is_default                    tinyint(1) default 0 not null,
  update_time                   bigint not null,
  interval_time                 integer not null,
  max_add                       double not null,
  min_add                       double not null,
  constraint pk_v1_vdeal_third_party primary key (id)
);

create table v1_vote (
  voe_id                        bigint auto_increment not null,
  voe_name                      varchar(255),
  voe_class                     integer not null,
  voe_price                     decimal(38),
  voe_interval                  integer not null,
  voe_item                      integer not null,
  voe_agree_total               integer not null,
  voe_reject_total              integer not null,
  voe_image                     varchar(255),
  voe_start                     bigint not null,
  voe_end                       bigint not null,
  voe_qualification_num         decimal(38),
  voe_qualification_class       integer not null,
  voe_desc                      varchar(255),
  voe_type                      integer not null,
  voe_status                    integer not null,
  voe_updated                   bigint not null,
  voe_created                   bigint not null,
  constraint pk_v1_vote primary key (voe_id)
);

create table v1_vote_item (
  vom_id                        bigint auto_increment not null,
  vom_voe_id                    bigint not null,
  vom_cuy_id                    integer not null,
  vom_type                      integer not null,
  vom_status                    integer not null,
  vom_agree                     integer not null,
  vom_reject                    integer not null,
  vom_updated                   bigint not null,
  vom_created                   bigint not null,
  constraint pk_v1_vote_item primary key (vom_id)
);

create table v1_vote_log (
  vog_id                        bigint auto_increment not null,
  vog_voe_id                    bigint not null,
  vog_vom_id                    bigint not null,
  vog_mer_id                    bigint not null,
  vog_type                      integer not null,
  vog_isagree                   integer not null,
  vog_created                   bigint not null,
  constraint pk_v1_vote_log primary key (vog_id)
);

create table v1_withdraw (
  wiw_id                        bigint auto_increment not null,
  wiw_mer_id                    bigint not null,
  wiw_bak_id                    bigint not null,
  wiw_bak_type                  integer not null,
  wiw_bak_province              varchar(255),
  wiw_bak_city                  varchar(255),
  wiw_bak_name                  varchar(255),
  wiw_bak_user                  varchar(255),
  wiw_bak_number                varchar(255),
  wiw_mer_balance               decimal(38),
  wiw_money                     decimal(38),
  wiw_money_collect             decimal(38),
  wiw_charge                    decimal(38),
  wiw_status                    integer not null,
  wiw_status_desc               varchar(255),
  wiw_updated                   bigint not null,
  wiw_created                   bigint not null,
  constraint pk_v1_withdraw primary key (wiw_id)
);

create table v1_withdraw_address (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  currency_id                   integer not null,
  address                       varchar(255),
  tag                           varchar(255),
  note                          varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_withdraw_address primary key (id)
);

alter table v1_currency_comment add constraint fk_v1_currency_comment_currency_cuy_id foreign key (currency_cuy_id) references v1_currency (cuy_id) on delete restrict on update restrict;

alter table v1_currency_comment add constraint fk_v1_currency_comment_member_mer_id foreign key (member_mer_id) references v1_member (mer_id) on delete restrict on update restrict;


# --- !Downs

alter table v1_currency_comment drop foreign key fk_v1_currency_comment_currency_cuy_id;

alter table v1_currency_comment drop foreign key fk_v1_currency_comment_member_mer_id;

drop table if exists cp_system_action;

drop table if exists v1_activity;

drop table if exists v1_activity_detail;

drop table if exists cp_member;

drop table if exists v1_article;

drop table if exists v1_article_category;

drop table if exists v1_balance_log_00;

drop table if exists v1_balance_person_log_00;

drop table if exists v1_bank;

drop table if exists v1_batch_trade_log;

drop table if exists v1_bittrex_currency_order;

drop table if exists v1_clear_data_log;

drop table if exists v1_coin_transfer_log;

drop table if exists v1_commission_white_list;

drop table if exists v1_exchange_contact_detail;

drop table if exists v1_crowd_fund_config;

drop table if exists v1_crowd_fund_level;

drop table if exists v1_crowd_fund_log;

drop table if exists v1_crowd_fund_project_reward_log;

drop table if exists v1_crowd_fund_reward_log;

drop table if exists v1_crowd_fund_schedule;

drop table if exists v1_crowd_fund_timeline;

drop table if exists v1_crowd_fund_unfreeze_log;

drop table if exists v1_currency;

drop table if exists v1_currency_comment;

drop table if exists v1_currency_entrust;

drop table if exists v1_currency_exchange_config;

drop table if exists v1_currency_kline;

drop table if exists v1_currency_link;

drop table if exists v1_currency_offer;

drop table if exists v1_currency_order;

drop table if exists v1_currency_vote;

drop table if exists v1_deposit;

drop table if exists v1_etc_in_log;

drop table if exists v1_gz_config;

drop table if exists v1_gz_invite_award_log;

drop table if exists v1_gz_invite_log;

drop table if exists v1_gz_log;

drop table if exists v1_gz_project;

drop table if exists v1_gz_unlock_quora;

drop table if exists v1_gz_stat;

drop table if exists v1_gz_invite_total;

drop table if exists v1_gz_stat_total;

drop table if exists v1_upgrade_grey_user;

drop table if exists cp_group;

drop table if exists cp_group_action;

drop table if exists cp_group_user;

drop table if exists v1_interest;

drop table if exists v1_interest_log;

drop table if exists v1_statistics_interest;

drop table if exists cp_log;

drop table if exists v1_login_log_00;

drop table if exists v1_lucky;

drop table if exists v1_lucky_config;

drop table if exists v1_lucky_log;

drop table if exists v1_member;

drop table if exists v1_member_balance;

drop table if exists v1_member_balance_old;

drop table if exists v1_member_level;

drop table if exists v1_member_like;

drop table if exists v1_member_old;

drop table if exists v1_member_score_config;

drop table if exists v1_member_score_log;

drop table if exists v1_two_auth_config;

drop table if exists v1_exchange_merchants;

drop table if exists v1_exchange_category;

drop table if exists v1_otc_ad;

drop table if exists v1_otc_coin;

drop table if exists v1_otc_complaint;

drop table if exists v1_otc_dealer;

drop table if exists v1_otc_order;

drop table if exists v1_offer;

drop table if exists v1_exchange_order;

drop table if exists v1_otc_black_list;

drop table if exists v1_overview;

drop table if exists v1_partner;

drop table if exists v1_partner_currency;

drop table if exists v1_partner_promotion;

drop table if exists v1_partner_promotion_trade;

drop table if exists v1_partner_stat;

drop table if exists v1_partner_sub;

drop table if exists v1_pay_log;

drop table if exists v1_point_card;

drop table if exists v1_point_card_config;

drop table if exists v1_point_card_log;

drop table if exists v1_point_card_transfer_log;

drop table if exists v1_exchange_price;

drop table if exists v1_promot;

drop table if exists v1_promot_log;

drop table if exists v1_promot_member;

drop table if exists v1_member_ready_balance;

drop table if exists v1_reg_activity;

drop table if exists v1_reg_activity_log;

drop table if exists v1_remittance;

drop table if exists v1_reward;

drop table if exists v1_robot_config;

drop table if exists v1_robot_log;

drop table if exists v1_robot_user;

drop table if exists v1_sms_log;

drop table if exists v1_statistics_balance;

drop table if exists v1_statistics_balance_log_00;

drop table if exists v1_statistics_money;

drop table if exists v1_statistics_overview;

drop table if exists v1_statistics_reg;

drop table if exists v1_statistics_vdeal;

drop table if exists v1_system_carousel;

drop table if exists v1_system;

drop table if exists v1_system_link;

drop table if exists v1_task_award;

drop table if exists v1_task_config;

drop table if exists v1_task_log;

drop table if exists v1_currency_kline_3rd_party;

drop table if exists v1_trade;

drop table if exists v1_trade_pair;

drop table if exists v1_trade_zone;

drop table if exists v1_currency_log;

drop table if exists v1_currency_outlog;

drop table if exists v1_exchange_treasure_log;

drop table if exists v1_exchange_treasure_number;

drop table if exists v1_upgrade_config;

drop table if exists v1_vdeal_black_list;

drop table if exists v1_vdeal_config;

drop table if exists v1_vdeal_currency_config;

drop table if exists v1_vdeal_config_interval;

drop table if exists v1_vdeal_log;

drop table if exists v1_vdeal_third_party;

drop table if exists v1_vote;

drop table if exists v1_vote_item;

drop table if exists v1_vote_log;

drop table if exists v1_withdraw;

drop table if exists v1_withdraw_address;

