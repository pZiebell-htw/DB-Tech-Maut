/*==============================================================*/
/* Table: BUCHUNG                                               */
/*==============================================================*/
create table BUCHUNG  (
                          BUCHUNG_ID           NUMBER(10)                      not null,
                          B_ID                 NUMBER(10)                      not null,
                          ABSCHNITTS_ID        NUMBER(6)                       not null,
                          KATEGORIE_ID         NUMBER(2)                       not null,
                          KENNZEICHEN          VARCHAR2(20)                    not null,
                          BUCHUNGSDATUM        TIMESTAMP                       not null,
                          BEFAHRUNGSDATUM      TIMESTAMP,
                          KOSTEN               NUMBER(8,2)                     not null,
                          constraint PK_BUCHUNG primary key (BUCHUNG_ID)
);

/*==============================================================*/
/* Index: BEINHALTET_FK                                         */
/*==============================================================*/
create index BEINHALTET_FK on BUCHUNG (
                                       ABSCHNITTS_ID ASC
    );

/*==============================================================*/
/* Index: HAT_FK                                                */
/*==============================================================*/
create index HAT_FK on BUCHUNG (
                                B_ID ASC
    );

/*==============================================================*/
/* Index: BEZIEHT_SICH_AUF_FK                                   */
/*==============================================================*/
create index BEZIEHT_SICH_AUF_FK on BUCHUNG (
                                             KATEGORIE_ID ASC
    );

/*==============================================================*/
/* Table: BUCHUNGSTATUS                                         */
/*==============================================================*/
create table BUCHUNGSTATUS  (
                                B_ID                 NUMBER(10)                      not null,
                                STATUS               VARCHAR2(15),
                                constraint PK_BUCHUNGSTATUS primary key (B_ID)
);

/*==============================================================*/
/* Table: FAHRZEUG                                              */
/*==============================================================*/
create table FAHRZEUG  (
                           FZ_ID                NUMBER(15)                      not null,
                           SSKL_ID              NUMBER(2)                       not null,
                           NUTZER_ID            NUMBER                          not null,
                           KENNZEICHEN          VARCHAR2(20),
                           FIN                  VARCHAR2(100),
                           ACHSEN               NUMBER(2),
                           GEWICHT              NUMBER(10,2),
                           ANMELDEDATUM         DATE,
                           ABMELDEDATUM         DATE,
                           ZULASSUNGSLAND       VARCHAR2(100),
                           constraint PK_FAHRZEUG primary key (FZ_ID)
);

/*==============================================================*/
/* Index: GEHOERT_FK                                            */
/*==============================================================*/
create index GEHOERT_FK on FAHRZEUG (
                                     NUTZER_ID ASC
    );

/*==============================================================*/
/* Index: FAEHRT_MIT_FK                                         */
/*==============================================================*/
create index FAEHRT_MIT_FK on FAHRZEUG (
                                        SSKL_ID ASC
    );

/*==============================================================*/
/* Table: FAHRZEUGGERAT                                         */
/*==============================================================*/
create table FAHRZEUGGERAT  (
                                FZG_ID               NUMBER(10)                      not null,
                                FZ_ID                NUMBER(15)                      not null,
                                STATUS               VARCHAR2(15),
                                TYP                  VARCHAR2(15),
                                EINBAUDATUM          DATE,
                                AUSBAUDATUM          DATE,
                                constraint PK_FAHRZEUGGERAT primary key (FZG_ID)
);

/*==============================================================*/
/* Index: RELATIONSHIP_3_FK                                     */
/*==============================================================*/
create index RELATIONSHIP_3_FK on FAHRZEUGGERAT (
                                                 FZ_ID ASC
    );

/*==============================================================*/
/* Table: MAUTABSCHNITT                                         */
/*==============================================================*/
create table MAUTABSCHNITT  (
                                ABSCHNITTS_ID        NUMBER(6)                       not null,
                                LAENGE               NUMBER(8,2),
                                START_KOORDINATE     VARCHAR2(20),
                                ZIEL_KOORDINATE      VARCHAR2(20),
                                NAME                 VARCHAR2(100),
                                ABSCHNITTSTYP        VARCHAR2(10),
                                constraint PK_MAUTABSCHNITT primary key (ABSCHNITTS_ID)
);

/*==============================================================*/
/* Table: MAUTERHEBUNG                                          */
/*==============================================================*/
create table MAUTERHEBUNG  (
                               MAUT_ID              NUMBER(10)                      not null,
                               ABSCHNITTS_ID        NUMBER(6)                       not null,
                               FZG_ID               NUMBER(10)                      not null,
                               KATEGORIE_ID         NUMBER(2)                       not null,
                               BEFAHRUNGSDATUM      TIMESTAMP                       not null,
                               KOSTEN               NUMBER(8,2)                     not null,
                               constraint PK_MAUTERHEBUNG primary key (MAUT_ID)
);

/*==============================================================*/
/* Index: MIT_FK                                                */
/*==============================================================*/
create index MIT_FK on MAUTERHEBUNG (
                                     ABSCHNITTS_ID ASC
    );

/*==============================================================*/
/* Index: DURCH_FK                                              */
/*==============================================================*/
create index DURCH_FK on MAUTERHEBUNG (
                                       FZG_ID ASC
    );

/*==============================================================*/
/* Index: BERECHNET_DURCH_FK                                    */
/*==============================================================*/
create index BERECHNET_DURCH_FK on MAUTERHEBUNG (
                                                 KATEGORIE_ID ASC
    );

/*==============================================================*/
/* Table: MAUTKATEGORIE                                         */
/*==============================================================*/
create table MAUTKATEGORIE  (
                                KATEGORIE_ID         NUMBER(2)                       not null,
                                SSKL_ID              NUMBER(2)                       not null,
                                KAT_BEZEICHNUNG      CHAR(1),
                                ACHSZAHL             VARCHAR2(5),
                                MAUTSATZ_JE_KM       NUMBER(10,3),
                                constraint PK_MAUTKATEGORIE primary key (KATEGORIE_ID)
);

/*==============================================================*/
/* Table: NUTZER                                                */
/*==============================================================*/
create table NUTZER  (
                         NUTZER_ID            NUMBER                          not null,
                         STATUS               VARCHAR2(15),
                         FIRMENNAME           VARCHAR2(100),
                         VORNAME              VARCHAR2(100),
                         NACHNAME             VARCHAR2(100),
                         LAND                 VARCHAR2(100),
                         STRASSE              VARCHAR2(100),
                         HAUSNUMMER           NUMBER,
                         POSTLEITZAHL         NUMBER,
                         RECHNUNGSINTERVALL   NUMBER,
                         RECHNUNGSZUSTELLUNG  VARCHAR2(10),
                         constraint PK_NUTZER primary key (NUTZER_ID)
);

/*==============================================================*/
/* Table: POSITION                                              */
/*==============================================================*/
create table POSITION  (
                           MAUT_ID              NUMBER(10)                      not null,
                           R_ID                 NUMBER(10)                      not null,
                           constraint PK_POSITION primary key (MAUT_ID, R_ID)
);

/*==============================================================*/
/* Index: POSITION_FK                                           */
/*==============================================================*/
create index POSITION_FK on POSITION (
                                      MAUT_ID ASC
    );

/*==============================================================*/
/* Index: POSITION2_FK                                          */
/*==============================================================*/
create index POSITION2_FK on POSITION (
                                       R_ID ASC
    );

/*==============================================================*/
/* Table: RECHNUNG                                              */
/*==============================================================*/
create table RECHNUNG  (
                           R_ID                 NUMBER(10)                      not null,
                           STATUS_ID            NUMBER                          not null,
                           NUTZER_ID            NUMBER                          not null,
                           BUCHUNGSDATUM        TIMESTAMP                       not null,
                           SUMME                NUMBER(8,2)                     not null,
                           constraint PK_RECHNUNG primary key (R_ID)
);

/*==============================================================*/
/* Index: AN_FK                                                 */
/*==============================================================*/
create index AN_FK on RECHNUNG (
                                NUTZER_ID ASC
    );

/*==============================================================*/
/* Index: RELATIONSHIP_12_FK                                    */
/*==============================================================*/
create index RELATIONSHIP_12_FK on RECHNUNG (
                                             STATUS_ID ASC
    );

/*==============================================================*/
/* Table: RECHNUNNGSSTATUS                                      */
/*==============================================================*/
create table RECHNUNNGSSTATUS  (
                                   STATUS_ID            NUMBER                          not null,
                                   STATUS_BEZ           VARCHAR2(20),
                                   constraint PK_RECHNUNNGSSTATUS primary key (STATUS_ID)
);

/*==============================================================*/
/* Table: SCHADSTOFFKLASSE                                      */
/*==============================================================*/
create table SCHADSTOFFKLASSE  (
                                   SSKL_ID              NUMBER(2)                       not null,
                                   MAUTSCHADSTOFFKLASSE VARCHAR2(10),
                                   BESCHREIBUNG         VARCHAR2(100),
                                   constraint PK_SCHADSTOFFKLASSE primary key (SSKL_ID)
);

/*==============================================================*/
/* Table: ZAHLART                                               */
/*==============================================================*/
create table ZAHLART  (
                          Z_ID                 CHAR(10)                        not null,
                          ZTYP_ID              NUMBER                          not null,
                          NUTZER_ID            NUMBER                          not null,
                          STATUS               VARCHAR2(15),
                          GUELTIG_VON          DATE,
                          GUELTIG_BIS          DATE,
                          EMMITENT             VARCHAR2(100),
                          BETRAG               NUMBER(10,2),
                          NAME                 VARCHAR2(100),
                          VORNAME              VARCHAR2(100),
                          NACHNAME             VARCHAR2(100),
                          BIN_CODE             NUMBER(5),
                          constraint PK_ZAHLART primary key (Z_ID)
);

/*==============================================================*/
/* Index: ZAHLT_MIT_FK                                          */
/*==============================================================*/
create index ZAHLT_MIT_FK on ZAHLART (
                                      NUTZER_ID ASC
    );

/*==============================================================*/
/* Index: GEHORT_ZU_FK                                          */
/*==============================================================*/
create index GEHORT_ZU_FK on ZAHLART (
                                      ZTYP_ID ASC
    );

/*==============================================================*/
/* Table: ZAHLTYP                                               */
/*==============================================================*/
create table ZAHLTYP  (
                          ZTYP_ID              NUMBER                          not null,
                          ZTYP_BEZEICHNUNG     VARCHAR2(50),
                          constraint PK_ZAHLTYP primary key (ZTYP_ID)
);

alter table BUCHUNG
    add constraint FK_BUCHUNG_BEINHALTE_MAUTABSC foreign key (ABSCHNITTS_ID)
        references MAUTABSCHNITT (ABSCHNITTS_ID);

alter table BUCHUNG
    add constraint FK_BUCHUNG_BEZIEHT_S_MAUTKATE foreign key (KATEGORIE_ID)
        references MAUTKATEGORIE (KATEGORIE_ID);

alter table BUCHUNG
    add constraint FK_BUCHUNG_HAT_BUCHUNGS foreign key (B_ID)
        references BUCHUNGSTATUS (B_ID);

alter table FAHRZEUG
    add constraint FK_FAHRZEUG_FAEHRT_MI_SCHADSTO foreign key (SSKL_ID)
        references SCHADSTOFFKLASSE (SSKL_ID);

alter table FAHRZEUG
    add constraint FK_FAHRZEUG_GEHOERT_NUTZER foreign key (NUTZER_ID)
        references NUTZER (NUTZER_ID);

alter table FAHRZEUGGERAT
    add constraint FK_FAHRZEUG_RELATIONS_FAHRZEUG foreign key (FZ_ID)
        references FAHRZEUG (FZ_ID);

alter table MAUTERHEBUNG
    add constraint FK_MAUTERHE_BERECHNET_MAUTKATE foreign key (KATEGORIE_ID)
        references MAUTKATEGORIE (KATEGORIE_ID);

alter table MAUTERHEBUNG
    add constraint FK_MAUTERHE_DURCH_FAHRZEUG foreign key (FZG_ID)
        references FAHRZEUGGERAT (FZG_ID);

alter table MAUTERHEBUNG
    add constraint FK_MAUTERHE_MIT_MAUTABSC foreign key (ABSCHNITTS_ID)
        references MAUTABSCHNITT (ABSCHNITTS_ID);

alter table MAUTKATEGORIE
    add constraint FK_MAUTKATE_BESTEHT_A_SCHADSTO foreign key (SSKL_ID)
        references SCHADSTOFFKLASSE (SSKL_ID);

alter table POSITION
    add constraint FK_POSITION_POSITION_MAUTERHE foreign key (MAUT_ID)
        references MAUTERHEBUNG (MAUT_ID);

alter table POSITION
    add constraint FK_POSITION_POSITION2_RECHNUNG foreign key (R_ID)
        references RECHNUNG (R_ID);

alter table RECHNUNG
    add constraint FK_RECHNUNG_AN_NUTZER foreign key (NUTZER_ID)
        references NUTZER (NUTZER_ID);

alter table RECHNUNG
    add constraint FK_RECHNUNG_RELATIONS_RECHNUNN foreign key (STATUS_ID)
        references RECHNUNNGSSTATUS (STATUS_ID);

alter table ZAHLART
    add constraint FK_ZAHLART_GEHORT_ZU_ZAHLTYP foreign key (ZTYP_ID)
        references ZAHLTYP (ZTYP_ID);

alter table ZAHLART
    add constraint FK_ZAHLART_ZAHLT_MIT_NUTZER foreign key (NUTZER_ID)
        references NUTZER (NUTZER_ID);
