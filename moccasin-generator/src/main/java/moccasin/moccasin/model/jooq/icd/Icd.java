/*
 * This file is generated by jOOQ.
*/
package moccasin.moccasin.model.jooq.icd;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import moccasin.moccasin.model.jooq.DefaultCatalog;
import moccasin.moccasin.model.jooq.icd.tables.Chapters;
import moccasin.moccasin.model.jooq.icd.tables.Groups;
import moccasin.moccasin.model.jooq.icd.tables.IcdCodes;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Icd extends SchemaImpl {

    private static final long serialVersionUID = -1650288932;

    /**
     * The reference instance of <code>icd</code>
     */
    public static final Icd ICD = new Icd();

    /**
     * The table <code>icd.chapters</code>.
     */
    public final Chapters CHAPTERS = moccasin.moccasin.model.jooq.icd.tables.Chapters.CHAPTERS;

    /**
     * The table <code>icd.groups</code>.
     */
    public final Groups GROUPS = moccasin.moccasin.model.jooq.icd.tables.Groups.GROUPS;

    /**
     * The table <code>icd.icd_codes</code>.
     */
    public final IcdCodes ICD_CODES = moccasin.moccasin.model.jooq.icd.tables.IcdCodes.ICD_CODES;

    /**
     * No further instances allowed
     */
    private Icd() {
        super("icd", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Chapters.CHAPTERS,
            Groups.GROUPS,
            IcdCodes.ICD_CODES);
    }
}
