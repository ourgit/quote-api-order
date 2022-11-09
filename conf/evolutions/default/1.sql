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
-- apply changes
create table v1_ad (
  id                            bigint auto_increment not null,
  position                      varchar(255),
  dimension                     varchar(255),
  price                         bigint not null,
  days                          integer not null,
  status                        integer not null,
  display                       varchar(255),
  source_type                   integer not null,
  page_type                     integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_ad primary key (id)
);

create table v1_ad_owner (
  id                            bigint auto_increment not null,
  ad_id                         bigint not null,
  position                      varchar(255),
  dimension                     varchar(255),
  begin_time                    bigint not null,
  end_time                      bigint not null,
  status                        integer not null,
  sort                          integer not null,
  source_url                    varchar(255),
  link_url                      varchar(255),
  uid                           bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_ad_owner primary key (id)
);

create table v1_ad_viewer (
  id                            bigint auto_increment not null,
  ad_id                         bigint not null,
  uid                           bigint not null,
  avatar                        varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_ad_viewer primary key (id)
);

create table v1_article (
  id                            bigint auto_increment not null,
  title                         varchar(255),
  author                        varchar(255),
  source                        varchar(255),
  cate_id                       integer not null,
  publish_time                  bigint not null,
  status                        integer not null,
  is_top                        tinyint(1) default 0 not null,
  is_recommend                  tinyint(1) default 0 not null,
  sort                          integer not null,
  content                       varchar(255),
  digest                        varchar(255),
  head_pic                      varchar(255),
  tags                          varchar(255),
  product_id_list               varchar(255),
  views                         bigint not null,
  favs                          bigint not null,
  comments                      bigint not null,
  shares                        bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_article primary key (id)
);

create table v1_article_category (
  id                            integer auto_increment not null,
  name                          varchar(255),
  sort                          integer not null,
  status                        integer not null,
  cate_type                     integer not null,
  note                          varchar(255),
  head_pic                      varchar(255),
  icon                          varchar(255),
  display_mode                  varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_article_category primary key (id)
);

create table v1_article_comment (
  id                            bigint auto_increment not null,
  content                       varchar(255),
  article_id                    bigint not null,
  uid                           bigint not null,
  likes                         bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_article_comment primary key (id)
);

create table v1_article_comment_like (
  id                            bigint auto_increment not null,
  comment_id                    bigint not null,
  has_like                      tinyint(1) default 0 not null,
  uid                           bigint not null,
  author_uid                    bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_article_comment_like primary key (id)
);

create table v1_article_comment_reply (
  id                            bigint auto_increment not null,
  content                       varchar(255),
  article_comment_id            bigint not null,
  at_uid                        bigint not null,
  uid                           bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_article_comment_reply primary key (id)
);

create table v1_article_fav (
  id                            bigint auto_increment not null,
  article_id                    bigint not null,
  uid                           bigint not null,
  head_pic                      varchar(255),
  author                        varchar(255),
  title                         varchar(255),
  enable                        tinyint(1) default 0 not null,
  article_time                  bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_article_fav primary key (id)
);

create table v1_balance_log (
  id                            bigint auto_increment not null,
  order_no                      varchar(255),
  uid                           bigint not null,
  item_id                       integer not null,
  left_balance                  bigint not null,
  freeze_balance                bigint not null,
  total_balance                 bigint not null,
  change_amount                 bigint not null,
  biz_type                      integer not null,
  note                          varchar(255),
  freeze_status                 integer not null,
  create_time                   bigint not null,
  constraint pk_v1_balance_log primary key (id)
);

create table v1_bid (
  id                            bigint auto_increment not null,
  service_region                varchar(255),
  service_address               varchar(255),
  category_name                 varchar(255),
  preference_service_time       varchar(255),
  service_content               varchar(255),
  contact_mail                  varchar(255),
  contact_name                  varchar(255),
  asker_uid                     bigint not null,
  asker_name                    varchar(255),
  status                        integer not null,
  taker_uid                     bigint not null,
  taker_name                    varchar(255),
  price                         bigint not null,
  category_id                   bigint not null,
  contact_phone_number          varchar(255),
  file_list                     varchar(255),
  lat                           double not null,
  lng                           double not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_bid primary key (id)
);

create table v1_bid_detail (
  id                            bigint auto_increment not null,
  bid_id                        bigint not null,
  bidder_uid                    bigint not null,
  bidder_name                   varchar(255),
  note                          varchar(255),
  price                         bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_bid_detail primary key (id)
);

create table v1_bid_user (
  id                            bigint auto_increment not null,
  user_name                     varchar(255),
  bid_id                        bigint not null,
  uid                           bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_bid_user primary key (id)
);

create table v1_category (
  id                            bigint auto_increment not null,
  parent_id                     bigint not null,
  name                          varchar(255),
  img_url                       varchar(255),
  poster                        varchar(255),
  path                          varchar(255),
  path_name                     varchar(255),
  pinyin_abbr                   varchar(255),
  seo_keyword                   varchar(255),
  seo_description               varchar(255),
  is_shown                      integer not null,
  cate_type                     integer not null,
  sort                          integer not null,
  sold_amount                   bigint not null,
  posts                         bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_category primary key (id)
);

create table v1_charge (
  id                            bigint auto_increment not null,
  transaction_id                varchar(255),
  sub_id                        varchar(255),
  uid                           bigint not null,
  amount                        integer not null,
  status                        integer not null,
  pay_type                      integer not null,
  update_time                   bigint not null,
  constraint pk_v1_charge primary key (id)
);

create table v1_image (
  id                            bigint auto_increment not null,
  image                         varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_image primary key (id)
);

create table v1_login_log (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  ip                            varchar(255),
  place                         varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_login_log primary key (id)
);

create table v1_member (
  id                            bigint auto_increment not null,
  login_password                varchar(255),
  pay_password                  varchar(255),
  status                        integer not null,
  real_name                     varchar(255),
  nick_name                     varchar(255),
  account_name                  varchar(255),
  contact_number                varchar(255),
  create_time                   bigint not null,
  avatar                        varchar(255),
  shop_id                       bigint not null,
  shop_name                     varchar(255),
  auth_status                   integer not null,
  user_type                     integer not null,
  expire_time                   bigint not null,
  update_time                   bigint not null,
  constraint pk_v1_member primary key (id)
);

create table v1_member_balance (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  item_id                       integer not null,
  left_balance                  bigint not null,
  freeze_balance                bigint not null,
  total_balance                 bigint not null,
  expire_time                   bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_member_balance primary key (id)
);

create table v1_member_ship (
  id                            bigint auto_increment not null,
  duration                      bigint not null,
  old_price                     bigint not null,
  price                         bigint not null,
  sort                          bigint not null,
  constraint pk_v1_member_ship primary key (id)
);

create table v1_msg (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  item_id                       integer not null,
  change_amount                 bigint not null,
  title                         varchar(255),
  content                       varchar(255),
  link_url                      varchar(255),
  msg_type                      integer not null,
  status                        integer not null,
  create_time                   bigint not null,
  constraint pk_v1_msg primary key (id)
);

create table v1_new_shop_factory (
  id                            bigint auto_increment not null,
  parent_id                     bigint not null,
  name                          varchar(255),
  img_url                       varchar(255),
  poster                        varchar(255),
  path                          varchar(255),
  pinyin_abbr                   varchar(255),
  is_shown                      integer not null,
  sort                          integer not null,
  sold_amount                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_new_shop_factory primary key (id)
);

create table v1_new_shop_plan (
  id                            integer auto_increment not null,
  name                          varchar(255),
  img_url                       varchar(255),
  link_url                      varchar(255),
  cover_url                     varchar(255),
  client_type                   integer not null,
  biz_type                      integer not null,
  sort                          integer not null,
  need_show                     tinyint(1) default 0 not null,
  title1                        varchar(255),
  title2                        varchar(255),
  note                          varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_new_shop_plan primary key (id)
);

create table v1_news_search (
  id                            bigint auto_increment not null,
  keyword                       varchar(255),
  views                         bigint not null,
  constraint pk_v1_news_search primary key (id)
);

create table v1_system_config (
  id                            integer auto_increment not null,
  config_key                    varchar(255),
  config_value                  varchar(255),
  note                          varchar(255),
  enable                        tinyint(1) default 0 not null,
  source                        integer not null,
  content_type                  integer not null,
  is_encrypt                    tinyint(1) default 0 not null,
  update_time                   bigint not null,
  constraint pk_v1_system_config primary key (id)
);

create table v1_post (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  user_name                     varchar(255),
  avatar                        varchar(255),
  title                         varchar(255),
  content                       varchar(255),
  category_id                   bigint not null,
  category_name                 varchar(255),
  comment_number                bigint not null,
  replies                       bigint not null,
  views                         bigint not null,
  participants                  bigint not null,
  status                        integer not null,
  place_top                     tinyint(1) default 0 not null,
  likes                         bigint not null,
  update_time                   bigint not null,
  last_reply_time               bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_post primary key (id)
);

create table v1_post_category (
  id                            bigint auto_increment not null,
  parent_id                     bigint not null,
  name                          varchar(255),
  img_url                       varchar(255),
  poster                        varchar(255),
  path                          varchar(255),
  path_name                     varchar(255),
  pinyin_abbr                   varchar(255),
  seo_keyword                   varchar(255),
  seo_description               varchar(255),
  is_shown                      integer not null,
  cate_type                     integer not null,
  sort                          integer not null,
  sold_amount                   bigint not null,
  posts                         bigint not null,
  create_time                   bigint not null,
  admin_list                    varchar(255),
  constraint pk_v1_post_category primary key (id)
);

create table v1_post_like (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  post_id                       bigint not null,
  like                          tinyint(1) default 0 not null,
  update_time                   bigint not null,
  constraint pk_v1_post_like primary key (id)
);

create table v1_reply (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  user_name                     varchar(255),
  avatar                        varchar(255),
  content                       varchar(255),
  post_title                    varchar(255),
  post_id                       bigint not null,
  quote_id                      bigint not null,
  replies                       bigint not null,
  status                        integer not null,
  likes                         bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_reply primary key (id)
);

create table v1_search_key_word (
  id                            integer auto_increment not null,
  key_word                      varchar(255),
  sort                          integer not null,
  constraint pk_v1_search_key_word primary key (id)
);

create table v1_service (
  id                            bigint auto_increment not null,
  service_name                  varchar(255),
  service_icon                  varchar(255),
  service_digest                varchar(255),
  service_content               varchar(255),
  shop_id                       bigint not null,
  category_id                   bigint not null,
  sort                          integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_service primary key (id)
);

create table v1_shop (
  id                            bigint auto_increment not null,
  status                        integer not null,
  run_type                      integer not null,
  shop_level                    integer not null,
  name                          varchar(255),
  digest                        varchar(255),
  contact_number                varchar(255),
  contact_name                  varchar(255),
  contact_address               varchar(255),
  license_number                varchar(255),
  license_img                   varchar(255),
  description                   varchar(255),
  approve_note                  varchar(255),
  log                           varchar(255),
  creator_id                    bigint not null,
  approver_id                   bigint not null,
  lat                           double not null,
  lon                           double not null,
  open_time                     integer not null,
  close_time                    integer not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  filter                        varchar(255),
  business_time                 varchar(255),
  avatar                        varchar(255),
  rect_logo                     varchar(255),
  product_counts                bigint not null,
  views                         bigint not null,
  tags                          varchar(255),
  images                        varchar(255),
  discount_str                  varchar(255),
  branches                      varchar(255),
  discount                      integer not null,
  bid_discount                  integer not null,
  average_consumption           integer not null,
  order_count                   bigint not null,
  sort                          integer not null,
  score                         integer not null,
  place_top                     tinyint(1) default 0 not null,
  bulletin                      varchar(255),
  env_images                    varchar(255),
  new_shop_category_id          varchar(255),
  product_description_score     integer not null,
  service_score                 integer not null,
  mail_score                    integer not null,
  total_money                   bigint not null,
  total_comment_count           bigint not null,
  apply_categories              varchar(255),
  apply_categories_name         varchar(255),
  qualifications                varchar(255),
  expire_time                   bigint not null,
  constraint pk_v1_shop primary key (id)
);

create table v1_shop_apply_log (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  shop_id                       bigint not null,
  shop_name                     varchar(255),
  user_name                     varchar(255),
  audit_note                    varchar(255),
  phone_number                  varchar(255),
  address                       varchar(255),
  digest                        varchar(255),
  business_items                varchar(255),
  auditor_uid                   bigint not null,
  auditor_name                  varchar(255),
  category_list                 varchar(255),
  images                        varchar(255),
  logo                          varchar(255),
  create_time                   bigint not null,
  audit_time                    bigint not null,
  status                        integer not null,
  constraint pk_v1_shop_apply_log primary key (id)
);

create table v1_shop_category (
  id                            bigint auto_increment not null,
  shop_id                       bigint not null,
  parent_id                     bigint not null,
  name                          varchar(255),
  path_name                     varchar(255),
  img_url                       varchar(255),
  poster                        varchar(255),
  path                          varchar(255),
  pinyin_abbr                   varchar(255),
  is_shown                      integer not null,
  sort                          integer not null,
  sold_amount                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_shop_category primary key (id)
);

create table v1_shop_profile (
  id                            bigint auto_increment not null,
  shop_id                       bigint not null,
  uid                           bigint not null,
  shop_name                     varchar(255),
  company_name                  varchar(255),
  license_number                varchar(255),
  license_img                   varchar(255),
  law_name                      varchar(255),
  law_contact_number            varchar(255),
  id_no                         varchar(255),
  id_card_front                 varchar(255),
  id_card_back                  varchar(255),
  contact_number                varchar(255),
  contact_name                  varchar(255),
  contact_address               varchar(255),
  digest                        varchar(255),
  qualifications                varchar(255),
  description                   varchar(255),
  approve_note                  varchar(255),
  apply_categories              varchar(255),
  apply_categories_name         varchar(255),
  open_bank                     varchar(255),
  open_user_name                varchar(255),
  open_account_name             varchar(255),
  open_license_img              varchar(255),
  update_time                   bigint not null,
  status                        integer not null,
  rect_logo                     varchar(255),
  transfer_proof                varchar(255),
  create_time                   bigint not null,
  constraint pk_v1_shop_profile primary key (id)
);

create table v1_shop_tag (
  id                            bigint auto_increment not null,
  shop_id                       bigint not null,
  tag                           varchar(255),
  constraint pk_v1_shop_tag primary key (id)
);

create table v1_shopping_cart (
  id                            bigint auto_increment not null,
  uid                           bigint not null,
  product_id                    bigint not null,
  sku_id                        bigint not null,
  amount                        bigint not null,
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_shopping_cart primary key (id)
);

create table v1_show_case (
  id                            bigint auto_increment not null,
  title                         varchar(255),
  tags                          varchar(255),
  images                        varchar(255),
  cover                         varchar(255),
  image_count                   bigint not null,
  category_id                   bigint not null,
  shop_name                     varchar(255),
  shop_logo                     varchar(255),
  content                       varchar(255),
  place_top                     tinyint(1) default 0 not null,
  shop_id                       bigint not null,
  status                        integer not null,
  sort                          integer not null,
  create_time                   bigint not null,
  constraint pk_v1_show_case primary key (id)
);

create table v1_sms_log (
  id                            bigint auto_increment not null,
  msg_id                        varchar(255),
  phone_number                  varchar(255),
  content                       varchar(255),
  extno                         varchar(255),
  req_status                    varchar(255),
  resp_status                   varchar(255),
  req_time                      bigint not null,
  resp_time                     bigint not null,
  query_resp_time               bigint not null,
  constraint pk_v1_sms_log primary key (id)
);

create table v1_system_carousel (
  id                            integer auto_increment not null,
  name                          varchar(255),
  img_url                       varchar(255),
  link_url                      varchar(255),
  mobile_img_url                varchar(255),
  mobile_link_url               varchar(255),
  type                          integer not null,
  sort                          integer not null,
  need_show                     tinyint(1) default 0 not null,
  title1                        varchar(255),
  title2                        varchar(255),
  note                          varchar(255),
  region_code                   varchar(255),
  region_name                   varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_system_carousel primary key (id)
);

create table v1_system_link (
  id                            integer auto_increment not null,
  name                          varchar(255),
  url                           varchar(255),
  sort                          integer not null,
  status                        integer not null,
  note                          varchar(255),
  update_time                   bigint not null,
  create_time                   bigint not null,
  constraint pk_v1_system_link primary key (id)
);


# --- !Downs

-- drop all
drop table if exists v1_ad;

drop table if exists v1_ad_owner;

drop table if exists v1_ad_viewer;

drop table if exists v1_article;

drop table if exists v1_article_category;

drop table if exists v1_article_comment;

drop table if exists v1_article_comment_like;

drop table if exists v1_article_comment_reply;

drop table if exists v1_article_fav;

drop table if exists v1_balance_log;

drop table if exists v1_bid;

drop table if exists v1_bid_detail;

drop table if exists v1_bid_user;

drop table if exists v1_category;

drop table if exists v1_charge;

drop table if exists v1_image;

drop table if exists v1_login_log;

drop table if exists v1_member;

drop table if exists v1_member_balance;

drop table if exists v1_member_ship;

drop table if exists v1_msg;

drop table if exists v1_new_shop_factory;

drop table if exists v1_new_shop_plan;

drop table if exists v1_news_search;

drop table if exists v1_system_config;

drop table if exists v1_post;

drop table if exists v1_post_category;

drop table if exists v1_post_like;

drop table if exists v1_reply;

drop table if exists v1_search_key_word;

drop table if exists v1_service;

drop table if exists v1_shop;

drop table if exists v1_shop_apply_log;

drop table if exists v1_shop_category;

drop table if exists v1_shop_profile;

drop table if exists v1_shop_tag;

drop table if exists v1_shopping_cart;

drop table if exists v1_show_case;

drop table if exists v1_sms_log;

drop table if exists v1_system_carousel;

drop table if exists v1_system_link;

