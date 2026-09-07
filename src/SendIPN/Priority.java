/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SendIPN;

/**
 *
 * @author andh
 */
public enum Priority {
    URGENT(2), NORMAL(0), NONE_URGENT(1), SS(0), DD(1), FF(2), GG(3), KK(4);
    
    private final int id;
    
    Priority(int id) { this.id = id;}
    
    public int getValue() { return this.id; }
}
