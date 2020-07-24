package org.terrier.structures;


public abstract class FieldedDocumentIndexEntry extends DocumentIndexEntry {

    public abstract int[] getFieldLengths();
    public abstract void setFieldLengths(int[] f_lens);
    
    public void setDocumentIndexStatistics(DocumentIndexEntry die) {
        super.setDocumentIndexStatistics(die);
        this.setFieldLengths(((FieldedDocumentIndexEntry) die).getFieldLengths());
        this.doclength = die.getDocumentLength();
        this.entries = die.getNumberOfEntries();
    }

}