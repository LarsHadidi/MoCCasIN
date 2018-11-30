/*
 * This file is generated by jOOQ.
*/
package moccasin.moccasin.model.jooq.icd.tables.records;


import javax.annotation.Generated;

import moccasin.moccasin.model.jooq.icd.tables.Groups;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


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
public class GroupsRecord extends UpdatableRecordImpl<GroupsRecord> implements Record4<String, String, String, String> {

    private static final long serialVersionUID = 775812126;

    /**
     * Setter for <code>icd.groups.grvon</code>.
     */
    public void setGrvon(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>icd.groups.grvon</code>.
     */
    public String getGrvon() {
        return (String) get(0);
    }

    /**
     * Setter for <code>icd.groups.grbis</code>.
     */
    public void setGrbis(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>icd.groups.grbis</code>.
     */
    public String getGrbis() {
        return (String) get(1);
    }

    /**
     * Setter for <code>icd.groups.kapnr</code>.
     */
    public void setKapnr(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>icd.groups.kapnr</code>.
     */
    public String getKapnr() {
        return (String) get(2);
    }

    /**
     * Setter for <code>icd.groups.grti</code>.
     */
    public void setGrti(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>icd.groups.grti</code>.
     */
    public String getGrti() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<String, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<String, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return Groups.GROUPS.GRVON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Groups.GROUPS.GRBIS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Groups.GROUPS.KAPNR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Groups.GROUPS.GRTI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getGrvon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getGrbis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getKapnr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getGrti();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getGrvon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getGrbis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getKapnr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getGrti();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupsRecord value1(String value) {
        setGrvon(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupsRecord value2(String value) {
        setGrbis(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupsRecord value3(String value) {
        setKapnr(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupsRecord value4(String value) {
        setGrti(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupsRecord values(String value1, String value2, String value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached GroupsRecord
     */
    public GroupsRecord() {
        super(Groups.GROUPS);
    }

    /**
     * Create a detached, initialised GroupsRecord
     */
    public GroupsRecord(String grvon, String grbis, String kapnr, String grti) {
        super(Groups.GROUPS);

        set(0, grvon);
        set(1, grbis);
        set(2, kapnr);
        set(3, grti);
    }
}
