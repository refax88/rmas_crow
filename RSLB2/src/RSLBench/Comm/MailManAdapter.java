/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Comm;

/**
 *
 * @author fabio
 */
import messages.MailMan;


public class MailManAdapter extends AbstractCom {
    //Lo static va dentro la classe non fuori
    public static MailMan _mailman = new MailMan(); //io qui inizializzerei subito l'oggetto mail man.
    //il costruttore richiama quello sopra e fa la stessa roba
    public MailManAdapter(int maxCommunicationRange) {
        super(maxCommunicationRange);
    } 
    
    public MailMan getMailMan() {
        return this._mailman;
    }
    
}
