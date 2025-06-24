alter table BUCHUNG
drop constraint FK_BUCHUNG_BEINHALTE_MAUTABSC;

alter table BUCHUNG
drop constraint FK_BUCHUNG_BEZIEHT_S_MAUTKATE;

alter table BUCHUNG
drop constraint FK_BUCHUNG_HAT_BUCHUNGS;

alter table FAHRZEUG
drop constraint FK_FAHRZEUG_FAEHRT_MI_SCHADSTO;

alter table FAHRZEUG
drop constraint FK_FAHRZEUG_GEHOERT_NUTZER;

alter table FAHRZEUGGERAT
drop constraint FK_FAHRZEUG_RELATIONS_FAHRZEUG;

alter table MAUTERHEBUNG
drop constraint FK_MAUTERHE_BERECHNET_MAUTKATE;

alter table MAUTERHEBUNG
drop constraint FK_MAUTERHE_DURCH_FAHRZEUG;

alter table MAUTERHEBUNG
drop constraint FK_MAUTERHE_MIT_MAUTABSC;

alter table MAUTKATEGORIE
drop constraint FK_MAUTKATE_BESTEHT_A_SCHADSTO;

alter table POSITION
drop constraint FK_POSITION_POSITION_MAUTERHE;

alter table POSITION
drop constraint FK_POSITION_POSITION2_RECHNUNG;

alter table RECHNUNG
drop constraint FK_RECHNUNG_AN_NUTZER;

alter table RECHNUNG
drop constraint FK_RECHNUNG_RELATIONS_RECHNUNN;

alter table ZAHLART
drop constraint FK_ZAHLART_GEHORT_ZU_ZAHLTYP;

alter table ZAHLART
drop constraint FK_ZAHLART_ZAHLT_MIT_NUTZER;

drop index BEZIEHT_SICH_AUF_FK;

drop index HAT_FK;

drop index BEINHALTET_FK;

drop table BUCHUNG cascade constraints;

drop table BUCHUNGSTATUS cascade constraints;

drop index FAEHRT_MIT_FK;

drop index GEHOERT_FK;

drop table FAHRZEUG cascade constraints;

drop index RELATIONSHIP_3_FK;

drop table FAHRZEUGGERAT cascade constraints;

drop table MAUTABSCHNITT cascade constraints;

drop index BERECHNET_DURCH_FK;

drop index DURCH_FK;

drop index MIT_FK;

drop table MAUTERHEBUNG cascade constraints;

drop table MAUTKATEGORIE cascade constraints;

drop table NUTZER cascade constraints;

drop index POSITION2_FK;

drop index POSITION_FK;

drop table POSITION cascade constraints;

drop index RELATIONSHIP_12_FK;

drop index AN_FK;

drop table RECHNUNG cascade constraints;

drop table RECHNUNNGSSTATUS cascade constraints;

drop table SCHADSTOFFKLASSE cascade constraints;

drop index GEHORT_ZU_FK;

drop index ZAHLT_MIT_FK;

drop table ZAHLART cascade constraints;

drop table ZAHLTYP cascade constraints;
