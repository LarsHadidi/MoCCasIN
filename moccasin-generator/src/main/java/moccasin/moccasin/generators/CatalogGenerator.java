package moccasin.moccasin.generators;

import moccasin.moccasin.model.TreeNode;
import moccasin.moccasin.model.jooq.atc.tables.records.AtcCodesRecord;
import moccasin.moccasin.model.jooq.icd.tables.records.ChaptersRecord;
import moccasin.moccasin.model.jooq.icd.tables.records.GroupsRecord;
import moccasin.moccasin.model.jooq.icd.tables.records.IcdCodesRecord;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static moccasin.moccasin.model.jooq.atc.Tables.ATC_CODES;
import static moccasin.moccasin.model.jooq.icd.Tables.*;


@Mojo(name = "generate")
public class CatalogGenerator extends AbstractMojo {
    @Parameter(property = "target-path", defaultValue = "")
    private String targetPath;
    @Parameter(property = "source-path", defaultValue = "")
    private String sourcePath;
    @Parameter(property = "icd-chapter-file", defaultValue = "")
    private String icdChaptersFile;
    @Parameter(property = "icd-group-file", defaultValue = "")
    private String icdGroupsFile;
    @Parameter(property = "icd-codes-file", defaultValue = "")
    private String icdCodesFile;
    @Parameter(property = "atc-codes-file", defaultValue = "")
    private String atcCodesFile;
    @Parameter(property = "icd-csv-separator", defaultValue = ";")
    private char icdCsvSeparator;
    @Parameter(property = "atc-csv-separator", defaultValue = ";")
    private char atcCsvSeparator;

    private List<TreeNode> icdTree;
    private List<TreeNode> atcTree;
    private DSLContext jooq;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Instant startTime = Instant.now();
            System.out.println("Creating Context");
            Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:catalogs;create=true", "", "");
            jooq = DSL.using(connection, SQLDialect.HSQLDB);
            System.out.println("Creating in-memory database");
            jooq.execute("create schema \"icd\"");
            jooq.execute("create schema \"atc\"");
            jooq.execute("create table \"icd\".\"chapters\"( \"kapnr\" varchar(2) not null constraint \"kapx\" primary key, \"kapti\" varchar(255))");
            jooq.execute("create table \"icd\".\"groups\"( \"grvon\" varchar(3) not null constraint \"grx\" primary key, \"grbis\" varchar(3), \"kapnr\" varchar(2) constraint \"kgrx\" references \"icd\".\"chapters\", \"grti\" varchar(255))");
            jooq.execute("create table \"icd\".\"icd_codes\"( \"ebene\" varchar(1), \"ort\" varchar(1), \"art\" varchar(1), \"kapnr\" varchar(2) constraint \"codeskapitel\" references \"icd\".\"chapters\", \"grvon\" varchar(3) constraint \"codesgruppen\" references \"icd\".\"groups\", \"code\" varchar(7) not null constraint \"codex\" primary key, \"normcode\" varchar(6) constraint \"normcodex\" unique, \"codeohnepunkt\" varchar(5) constraint \"codeohnepunktx\" unique, \"titel\" varchar(255), \"dreisteller\" varchar(255), \"viersteller\" varchar(255), \"fünfsteller\" varchar(255), \"p295\" varchar(1), \"p301\" varchar(1), \"mortl1code\" varchar(5), \"mortl2code\" varchar(5), \"mortl3code\" varchar(5), \"mortl4code\" varchar(5), \"morblcode\" varchar(5), \"sexcode\" varchar(1), \"sexfehlertyp\" varchar(1), \"altuntneu\" varchar(4), \"altobneu\" varchar(4), \"altfehlertyp\" varchar(1), \"exot\" varchar(1), \"belegt\" varchar(1), \"ifsgmeldung\" varchar(1), \"ifsglabor\" varchar(1))");
            jooq.execute("create index \"ebenex\" on \"icd\".\"icd_codes\" (\"ebene\")");
            jooq.execute("create table \"atc\".\"atc_codes\"( \"ATC-Code\" longvarchar not null constraint \"atcx\" primary key, \"bedeutung\" longvarchar, \"DDD-Info\" longvarchar)");
            jooq.execute("create index \"codes_ATC-Code_index\" on \"atc\".\"atc_codes\" (\"ATC-Code\")");
            System.out.println("Loading data");
            jooq.loadInto(CHAPTERS).loadCSV(new File(sourcePath, icdChaptersFile)).fields(CHAPTERS.fields()).separator(icdCsvSeparator).ignoreRows(0).execute();
            jooq.loadInto(GROUPS).loadCSV(new File(sourcePath, icdGroupsFile)).fields(GROUPS.fields()).separator(icdCsvSeparator).ignoreRows(0).execute();
            jooq.loadInto(ICD_CODES).loadCSV(new File(sourcePath, icdCodesFile)).fields(ICD_CODES.fields()).separator(icdCsvSeparator).ignoreRows(0).execute();
            jooq.loadInto(ATC_CODES).loadCSV(new File(sourcePath, atcCodesFile)).fields(ATC_CODES.fields()).separator(atcCsvSeparator).ignoreRows(0).execute();
            jooq.settings().withUpdatablePrimaryKeys(true);
            jooq.selectFrom(ATC_CODES).fetch().forEach(atcCodesRecord -> {
                atcCodesRecord.setAtcCode(atcCodesRecord.getAtcCode().trim());
                atcCodesRecord.setBedeutung(atcCodesRecord.getBedeutung().trim());
                atcCodesRecord.setDddInfo(atcCodesRecord.getDddInfo().trim());
                atcCodesRecord.store();
            });
            jooq.settings().withUpdatablePrimaryKeys(false);
            System.out.println("Transforming Data");
            buildIcdCatalog();
            buildAtcCatalog();
            System.out.println("Writing Tree-Files");
            writeTree(new File(targetPath, "icd"), icdTree);
            writeTree(new File(targetPath, "atc"), atcTree);
            System.out.printf("Finished in %d ms%n", Duration.between(startTime, Instant.now()).toMillis());
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void buildIcdCatalog() {
        List<ChaptersRecord> kapitelRecordList;
        List<GroupsRecord> gruppenRecordList;
        List<IcdCodesRecord> dreistellerRecordList, vierstellerRecordList, fünfstellerRecordList;
        List<TreeNode> gruppen, dreisteller, viersteller, fünfsteller;
        icdTree = new ArrayList<>();

        kapitelRecordList = jooq.selectFrom(CHAPTERS).fetch().sortAsc(CHAPTERS.KAPNR);

        int currentChapter = 0;
        for(ChaptersRecord kapitelRecord : kapitelRecordList){
            System.out.printf("ICD Chapter %d out of %d%n", ++currentChapter, kapitelRecordList.size());

            gruppenRecordList = jooq.selectFrom(GROUPS).where(GROUPS.KAPNR.eq(kapitelRecord.getKapnr())).fetch().sortAsc(GROUPS.GRVON);
            gruppen = new ArrayList<>();
            TreeNode kapitelNode = new TreeNode(null, kapitelRecord.getKapnr(), kapitelRecord.getKapti(), 1, gruppenRecordList.size() == 0);

            for(GroupsRecord gruppenRecord : gruppenRecordList) {
                dreistellerRecordList = jooq.selectFrom(ICD_CODES).where(ICD_CODES.EBENE.eq("3")).and(ICD_CODES.GRVON.eq(gruppenRecord.getGrvon())).fetch().sortAsc(ICD_CODES.CODEOHNEPUNKT);
                dreisteller = new ArrayList<>();
                TreeNode gruppeNode = new TreeNode(kapitelNode, gruppenRecord.getGrvon() + "-" + gruppenRecord.getGrbis(), gruppenRecord.getGrti(), 2, dreistellerRecordList.size() == 0);

                for(IcdCodesRecord dreistellerRecord : dreistellerRecordList) {
                    vierstellerRecordList = jooq.selectFrom(ICD_CODES).where(ICD_CODES.EBENE.eq("4")).and(ICD_CODES.GRVON.eq(gruppenRecord.getGrvon())).and(ICD_CODES.CODEOHNEPUNKT.startsWith(dreistellerRecord.getCodeohnepunkt())).fetch().sortAsc(ICD_CODES.CODEOHNEPUNKT);
                    viersteller = new ArrayList<>();
                    TreeNode dreistellerNode = new TreeNode(gruppeNode, dreistellerRecord.getCode(), dreistellerRecord.getTitel(), 3, dreistellerRecord.getOrt().equals("T"));

                    for(IcdCodesRecord vierstellerRecord : vierstellerRecordList) {
                        fünfstellerRecordList = jooq.selectFrom(ICD_CODES).where(ICD_CODES.EBENE.eq("5")).and(ICD_CODES.GRVON.eq(gruppenRecord.getGrvon())).and(ICD_CODES.CODEOHNEPUNKT.startsWith(vierstellerRecord.getCodeohnepunkt())).fetch().sortAsc(ICD_CODES.CODEOHNEPUNKT);
                        fünfsteller = new ArrayList<>();
                        TreeNode vierstellerNode = new TreeNode(dreistellerNode, vierstellerRecord.getCode(), vierstellerRecord.getTitel(), 4, vierstellerRecord.getOrt().equals("T"));

                        for(IcdCodesRecord fünfstellerRecord : fünfstellerRecordList) {
                            TreeNode fünfstellerNode = new TreeNode(vierstellerNode, fünfstellerRecord.getCode(), fünfstellerRecord.getTitel(), 5, fünfstellerRecord.getOrt().equals("T"));
                            fünfsteller.add(fünfstellerNode);
                        }

                        vierstellerNode.setChildren(fünfsteller);
                        viersteller.add(vierstellerNode);
                    }

                    dreistellerNode.setChildren(viersteller);
                    dreisteller.add(dreistellerNode);
                }

                gruppeNode.setChildren(dreisteller);
                gruppen.add(gruppeNode);
            }

            kapitelNode.setChildren(gruppen);
            icdTree.add(kapitelNode);
        }
    }

    private void buildAtcCatalog() {
        List<AtcCodesRecord> anaGrpRecordList, theraGrpRecordList, theraSubGrpRecordList, chemGrpRecordList, chemSubGrpRecordList;
        List<TreeNode> theraGrp, theraSubGrp, chemGrp, chemSubGrp;
        atcTree = new ArrayList<>();

        anaGrpRecordList = jooq.selectFrom(ATC_CODES).where(ATC_CODES.ATC_CODE.length().eq(1)).fetch().sortAsc(ATC_CODES.ATC_CODE);
        int currentAnaGrp = 0;
        for (AtcCodesRecord anaGrpRecord : anaGrpRecordList) {
            System.out.printf("ATC anatomic group %d out of %d%n", ++currentAnaGrp, anaGrpRecordList.size());

            theraGrpRecordList = jooq.selectFrom(ATC_CODES).where(ATC_CODES.ATC_CODE.length().eq(3)).and(ATC_CODES.ATC_CODE.startsWith(anaGrpRecord.getAtcCode())).fetch().sortAsc(ATC_CODES.ATC_CODE);
            theraGrp = new ArrayList<>();
            TreeNode anaGrpNode = new TreeNode(null, anaGrpRecord.getAtcCode(), anaGrpRecord.getBedeutung(), 1, theraGrpRecordList.size() == 0);

            for(AtcCodesRecord theraGrpRecord : theraGrpRecordList) {
                theraSubGrpRecordList = jooq.selectFrom(ATC_CODES).where(ATC_CODES.ATC_CODE.length().eq(4)).and(ATC_CODES.ATC_CODE.startsWith(theraGrpRecord.getAtcCode())).fetch().sortAsc(ATC_CODES.ATC_CODE);
                theraSubGrp = new ArrayList<>();
                TreeNode theraGrpNode = new TreeNode(anaGrpNode, theraGrpRecord.getAtcCode(), theraGrpRecord.getBedeutung(), 2, theraSubGrpRecordList.size() == 0);

                for(AtcCodesRecord theraSubGrpRecord : theraSubGrpRecordList) {
                    chemGrpRecordList = jooq.selectFrom(ATC_CODES).where(ATC_CODES.ATC_CODE.length().eq(5)).and(ATC_CODES.ATC_CODE.startsWith(theraSubGrpRecord.getAtcCode())).fetch().sortAsc(ATC_CODES.ATC_CODE);
                    chemGrp = new ArrayList<>();
                    TreeNode theraSubGrpNode = new TreeNode(theraGrpNode, theraSubGrpRecord.getAtcCode(), theraSubGrpRecord.getBedeutung(), 3, chemGrpRecordList.size() == 0);

                    for(AtcCodesRecord chemGrpRecord : chemGrpRecordList) {
                        chemSubGrpRecordList = jooq.selectFrom(ATC_CODES).where(ATC_CODES.ATC_CODE.length().eq(7)).and(ATC_CODES.ATC_CODE.startsWith(chemGrpRecord.getAtcCode())).fetch().sortAsc(ATC_CODES.ATC_CODE);
                        chemSubGrp = new ArrayList<>();
                        TreeNode chemGrpNode = new TreeNode(theraSubGrpNode, chemGrpRecord.getAtcCode(), chemGrpRecord.getBedeutung(), 4, chemSubGrpRecordList.size() == 0);

                        for(AtcCodesRecord chemSubGrpRecord : chemSubGrpRecordList) {
                            TreeNode chemSubGrpNode = new TreeNode(chemGrpNode, chemSubGrpRecord.getAtcCode(), chemSubGrpRecord.getBedeutung(), 5, true);
                            chemSubGrpNode.addMetadataEntry("DDD-INFO", chemSubGrpRecord.getDddInfo().trim());
                            chemSubGrp.add(chemSubGrpNode);
                        }

                        chemGrpNode.setChildren(chemSubGrp);
                        chemGrp.add(chemGrpNode);
                    }

                    theraSubGrpNode.setChildren(chemGrp);
                    theraSubGrp.add(theraSubGrpNode);
                }

                theraGrpNode.setChildren(theraSubGrp);
                theraGrp.add(theraGrpNode);
            }

            anaGrpNode.setChildren(theraGrp);
            atcTree.add(anaGrpNode);
        }
    }

    private void writeTree(File catalogFile, List<TreeNode> catalog) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(catalogFile)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(catalog);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
