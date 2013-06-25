package sample;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.EntityID;

public class SimpleProtocol
{
    public static final byte POS_MESSAGE = 0x10;
    public static final byte CIVILIAN_OBSERVATION_MESSAGE = 0x11;
    public static final byte STATION_ASSIGNMENT_MESSAGE = 0x20;

    static public byte[] getPosMessage(Human agent)
    {
        int pos = agent.getPosition().getValue();
        byte[] posB = intToByteArray(pos);
        byte[] message = new byte[1 + 1 * 4];
        message[0] = POS_MESSAGE;
        for (int i = 0; i < posB.length; i++)
            message[i + 1] = posB[i];
        Logger.debugColor("Sending POSITION: Agent " + agent.getID().getValue() + " located at " + pos, Logger.FG_GREEN);
        return message;
    }

    static public int getPosIdFromMessage(byte[] message)
    {
        return byteArrayToInt(removeHeader(message));
    }

    static public byte[] removeHeader(byte[] message)
    {
        byte[] content = new byte[message.length - 1];
        for (int i = 0; i < content.length; i++)
            content[i] = message[i + 1];
        return content;
    }

    static public String toString(byte[] message)
    {
        int[] array = byteArrayToIntArray(message, false);
        return array.toString();
    }

    /*
     * Convert INT value to BYTE array
     */
    public static final byte[] intToByteArray(int value)
    {
        return new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
    }

    /*
     * Convert BYTE array to INT value
     */
    public static final int byteArrayToInt(byte[] b)
    {
        return (b[0] << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
    }

    /*
     * Convert BYTE array to INT array - Here we use 4 byte for each int
     */
    public static final int[] byteArrayToIntArray(byte[] b, boolean ignoreFirstByte)
    {
        int length = b.length / 4;
        int startOffs = 0;
        if (ignoreFirstByte)
        {
            startOffs = 1;
            length = (b.length - 1) / 4;
        }
        int[] intarray = new int[length];
        for (int i = 0; i < length; i++)
        {
            int offs = startOffs + i * 4;
            intarray[i] = (b[offs + 0] << 24) | ((b[offs + 1] & 0xFF) << 16) | ((b[offs + 2] & 0xFF) << 8) | (b[offs + 3] & 0xFF);
        }
        return intarray;
    }

    /*
     * Convert BYTE array to INT array - Here we use 4 byte for each int
     */
    public static final byte[] intArrayToByteArray(int[] iarray, int startIndexOffset)
    {
        int length = iarray.length;
        byte[] b = new byte[length * 4 + startIndexOffset];
        for (int i = 0; i < length; i++)
        {
            int value = iarray[i];
            int offs = startIndexOffset + i * 4;
            b[offs + 0] = (byte) (value >> 24);
            b[offs + 1] = (byte) (value >> 16);
            b[offs + 2] = (byte) (value >> 8);
            b[offs + 3] = (byte) value;
        }
        return b;
    }

    public static byte[] buildAssignmentMessage(ArrayList<EntityID> agents, ArrayList<Building> targets, int[] assignment, boolean addHeader)
    {
        if (targets.size() == 0 || agents.size() == 0)
            return null;

        // Convert to rescue IDs
        int agentIDs[] = new int[agents.size()];
        int targetIDs[] = new int[targets.size()];
        int c = 0;
        for (Iterator<Building> it = targets.iterator(); it.hasNext();)
        {
            targetIDs[c] = ((Building) it.next()).getID().getValue();
            c++;
        }
        Iterator<EntityID> it3 = agents.iterator();
        c = 0;
        while (it3.hasNext())
        {
            agentIDs[c] = (Integer) (it3.next()).getValue();
            c++;
        }

        // Build the allocation message
        int[] intArray = new int[agents.size() * 2];
        for (int i = 0; i < agents.size(); i++)
        {
            if (assignment[i] == -1)
                continue;

            // Find target ID
            int agentID = agentIDs[i];
            int targetID = targetIDs[assignment[i]];
            Logger.debugColor("A " + agentID + " ---> T " + targetID + "     (" + i + "  -->    " + assignment[i] + ")", Logger.BG_BLUE);
            intArray[i * 2] = agentID;
            intArray[i * 2 + 1] = targetID;
        }
        if (addHeader)
        {
            byte[] bArray = SimpleProtocol.intArrayToByteArray(intArray, 1);
            bArray[0] = SimpleProtocol.STATION_ASSIGNMENT_MESSAGE;
            return bArray;
        }
        else
        {
            return SimpleProtocol.intArrayToByteArray(intArray, 0);
        }
    }
    
    public static byte[] buildCivilianObservationMessage(Set<Civilian> civilians, int time)
    {
        int[] buffer = new int[civilians.size() * 3 + 1];
        buffer[0] = time;
        int index = 1;
        for (Civilian civilian: civilians)
        {
            buffer[index] = civilian.getID().getValue();
            buffer[index + 1] = civilian.getHP();
            buffer[index + 2] = civilian.getDamage();
            index += 3;
        }
        byte[] message = SimpleProtocol.intArrayToByteArray(buffer, 1);
        message[0] = SimpleProtocol.CIVILIAN_OBSERVATION_MESSAGE;
        return message;
    }

    public static Set<CivilianUpdate> parseCivilianObservationMessage(byte[] message)
    {
        int[] buffer = byteArrayToIntArray(message, true);
        int time = buffer [0];
        Set<CivilianUpdate> updates = new HashSet<CivilianUpdate>();
        for(int index = 1; index < buffer.length; index+=3)
        {
            updates.add(new CivilianUpdate(buffer[index], buffer[index + 1], buffer[index + 2], time));
        }
        return updates;
    }
    
//    public static void main(String[] args)
//    {
//        Set<Civilian> civs = new HashSet<Civilian>();
//        Civilian civ = new Civilian(new EntityID(0));
//        civ.setHP(10000);
//        civ.setDamage(250);
//        civs.add(civ);
//        byte[] msg = buildCivilianObservationMessage(civs, 3);
//        Set<CivilianUpdate> updates = parseCivilianObservationMessage(msg);
//        for(CivilianUpdate update: updates)
//        {
//            System.out.println("civ: hp="+update.getHitPoints() + " dmg="+update.getDamage());
//        }
//    }
}
