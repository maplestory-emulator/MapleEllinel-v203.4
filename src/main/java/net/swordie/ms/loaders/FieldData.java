package net.swordie.ms.loaders;

import net.swordie.ms.client.character.runestones.RuneStone;
import net.swordie.ms.world.field.*;
import net.swordie.ms.life.Life;
import net.swordie.ms.life.npc.Npc;
import net.swordie.ms.life.Reactor;
import net.swordie.ms.ServerConstants;
import org.apache.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import net.swordie.ms.util.Position;
import net.swordie.ms.util.Util;
import net.swordie.ms.util.XMLApi;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 12/21/2017.
 */
public class FieldData {

    private static List<Field> fields = new ArrayList<>();
    private static final org.apache.log4j.Logger log = LogManager.getRootLogger();

    public static void main(String[] args) {
        generateDatFiles();
    }

    private static void saveFields(String dir) {
        Util.makeDirIfAbsent(dir);
            for (Field field : getFields()) {
                File file = new File(String.format("%s/%d.dat", dir, field.getId()));
                try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file))) {
                    dataOutputStream.writeInt(field.getId());
                    dataOutputStream.writeBoolean(field.isTown());
                    dataOutputStream.writeBoolean(field.isSwim());
                    dataOutputStream.writeInt(field.getReturnMap());
                    dataOutputStream.writeInt(field.getForcedReturn());
                    dataOutputStream.writeDouble(field.getMobRate());
                    dataOutputStream.writeBoolean(field.isFly());
                    dataOutputStream.writeUTF(field.getOnFirstUserEnter());
                    dataOutputStream.writeUTF(field.getOnUserEnter());
                    dataOutputStream.writeUTF(field.getFieldScript());
                    dataOutputStream.writeBoolean(field.isReactorShuffle());
                    dataOutputStream.writeBoolean(field.isExpeditionOnly());
                    dataOutputStream.writeBoolean(field.isPartyOnly());
                    dataOutputStream.writeBoolean(field.isNeedSkillForFly());
                    dataOutputStream.writeInt(field.getFixedMobCapacity());
                    dataOutputStream.writeInt(field.getCreateMobInterval());
                    dataOutputStream.writeInt(field.getTimeOut());
                    dataOutputStream.writeInt(field.getTimeLimit());
                    dataOutputStream.writeInt(field.getLvLimit());
                    dataOutputStream.writeInt(field.getLvForceMove());
                    dataOutputStream.writeInt(field.getConsumeItemCoolTime());
                    dataOutputStream.writeInt(field.getLink());
                    dataOutputStream.writeInt(field.getBossMobID());
                    dataOutputStream.writeInt(field.getVrTop());
                    dataOutputStream.writeInt(field.getVrLeft());
                    dataOutputStream.writeInt(field.getVrBottom());
                    dataOutputStream.writeInt(field.getVrRight());
                    dataOutputStream.writeShort(field.getFootholds().size());
                    for (Foothold fh : field.getFootholds()) {
                        dataOutputStream.writeInt(fh.getId());
                        dataOutputStream.writeInt(fh.getLayerId());
                        dataOutputStream.writeInt(fh.getGroupId());
                        dataOutputStream.writeInt(fh.getX1());
                        dataOutputStream.writeInt(fh.getY1());
                        dataOutputStream.writeInt(fh.getX2());
                        dataOutputStream.writeInt(fh.getY2());
                        dataOutputStream.writeInt(fh.getNext());
                        dataOutputStream.writeInt(fh.getPrev());
                        dataOutputStream.writeInt(fh.getForce());
                        dataOutputStream.writeBoolean(fh.isForbidFallDown());
                    }
                    dataOutputStream.writeShort(field.getPortals().size());
                    for (Portal p : field.getPortals()) {
                        dataOutputStream.writeInt(p.getId());
                        dataOutputStream.writeInt(p.getType().getVal());
                        dataOutputStream.writeUTF(p.getName());
                        dataOutputStream.writeInt(p.getTargetMapId());
                        dataOutputStream.writeUTF(p.getTargetPortalName());
                        dataOutputStream.writeInt(p.getX());
                        dataOutputStream.writeInt(p.getY());
                        dataOutputStream.writeInt(p.getHorizontalImpact());
                        dataOutputStream.writeInt(p.getVerticalImpact());
                        dataOutputStream.writeUTF(p.getScript());
                        dataOutputStream.writeBoolean(p.isOnlyOnce());
                        dataOutputStream.writeInt(p.getDelay());
                    }
                    dataOutputStream.writeShort(field.getLifes().size());
                    for (Life l : field.getLifes()) {
                        dataOutputStream.writeUTF(l.getLifeType());
                        dataOutputStream.writeInt(l.getTemplateId());
                        dataOutputStream.writeInt(l.getX());
                        dataOutputStream.writeInt(l.getY());
                        dataOutputStream.writeInt(l.getMobTime());
                        dataOutputStream.writeInt(l.getFlip());
                        dataOutputStream.writeBoolean(l.isHide());
                        dataOutputStream.writeInt(l.getFh());
                        dataOutputStream.writeInt(l.getCy());
                        dataOutputStream.writeInt(l.getRx0());
                        dataOutputStream.writeInt(l.getRx1());
                        dataOutputStream.writeUTF(l.getLimitedName());
                        dataOutputStream.writeBoolean(l.isUseDay());
                        dataOutputStream.writeBoolean(l.isUseNight());
                        dataOutputStream.writeBoolean(l.isHold());
                        dataOutputStream.writeBoolean(l.isNoFoothold());
                        dataOutputStream.writeBoolean(l.isDummy());
                        dataOutputStream.writeBoolean(l.isSpine());
                        dataOutputStream.writeBoolean(l.isMobTimeOnDie());
                        dataOutputStream.writeInt(l.getRegenStart());
                        dataOutputStream.writeInt(l.getMobAliveReq());
                    }
                    dataOutputStream.writeShort(field.getReactors().size());
                    for (Reactor r : field.getReactors()) {
                        dataOutputStream.writeInt(r.getTemplateId());
                        dataOutputStream.writeShort(r.getHomePosition().getX());
                        dataOutputStream.writeShort(r.getHomePosition().getY());
                        dataOutputStream.writeInt(r.getReactorTime());
                        dataOutputStream.writeInt(r.getFlip());
                        dataOutputStream.writeUTF(r.getName());
                        dataOutputStream.writeBoolean(r.isPhantomForest());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    private static void loadFieldInfoFromWz() {
        String wzDir = ServerConstants.WZ_DIR + "/Map.wz/Map";
        File dir = new File(wzDir);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.listFiles() == null) {
                continue;
            }
            for (File mapFile : file.listFiles()) {
                Document doc = XMLApi.getRoot(mapFile);
                Node node = XMLApi.getAllChildren(doc).get(0);
                if (node == null) {
                    continue;
                }
                int id = Integer.parseInt(XMLApi.getAttributes(node).get("name").replace(".img", ""));
                Field field = new Field(id, -1);
                Node infoNode = XMLApi.getFirstChildByNameBF(node, "info");
                for (Node n : XMLApi.getAllChildren(infoNode)) {
                    Map<String, String> attr = XMLApi.getAttributes(n);
                    String name = attr.get("name");
                    String value = attr.get("value");
                    switch (name) {
                        case "town":
                            field.setTown(Integer.parseInt(value) != 0);
                            break;
                        case "swim":
                            field.setSwim(Integer.parseInt(value) != 0);
                            break;
                        case "returnMap":
                            field.setReturnMap(Integer.parseInt(value));
                            break;
                        case "forcedReturn":
                            field.setForcedReturn(Integer.parseInt(value));
                            break;
                        case "mobRate":
                            field.setMobRate(Double.parseDouble(value));
                            break;
                        case "fly":
                            field.setFly(Integer.parseInt(value) != 0);
                            break;
                        case "onFirstUserEnter":
                            field.setOnFirstUserEnter(value);
                            break;
                        case "onUserEnter":
                            field.setOnUserEnter(value);
                            break;
                        case "fieldScript":
                            field.setFieldScript(value);
                            break;
                        case "reactorShuffle":
                            field.setReactorShuffle(Integer.parseInt(value) != 0);
                            break;
                        case "expeditionOnly":
                            field.setExpeditionOnly(Integer.parseInt(value) != 0);
                            break;
                        case "partyOnly":
                            field.setPartyOnly(Integer.parseInt(value) != 0);
                            break;
                        case "isNeedSkillForFly":
                            field.setNeedSkillForFly(Integer.parseInt(value) != 0);
                            break;
                        case "fixedMobCapacity":
                            field.setFixedMobCapacity(Integer.parseInt(value));
                            break;
                        case "createMobInterval":
                            field.setCreateMobInterval(Integer.parseInt(value));
                            break;
                        case "timeOut":
                            field.setTimeOut(Integer.parseInt(value));
                            break;
                        case "timeLimit":
                            field.setTimeLimit(Integer.parseInt(value));
                            break;
                        case "lvLimit":
                            field.setLvLimit(Integer.parseInt(value));
                            break;
                        case "lvForceMove":
                            field.setLvForceMove(Integer.parseInt(value));
                            break;
                        case "consumeItemCoolTime":
                            field.setConsumeItemCoolTime(Integer.parseInt(value));
                            break;
                        case "link":
                            field.setLink(Integer.parseInt(value));
                            break;
                        case "bossMobID":
                            field.setBossMobID(Integer.parseInt(value));
                            break;
                        case "VRTop":
                            field.setVrTop(Integer.parseInt(value));
                            break;
                        case "VRLeft":
                            field.setVrLeft(Integer.parseInt(value));
                            break;
                        case "VRBottom":
                            field.setVrBottom(Integer.parseInt(value));
                            break;
                        case "VRRight":
                            field.setVrRight(Integer.parseInt(value));
                            break;
                    }
                }
                Node fhNode = XMLApi.getFirstChildByNameBF(node, "foothold");
                if (fhNode != null) {
                    for (Node layerIDNode : XMLApi.getAllChildren(fhNode)) {
                        int layerID = Integer.parseInt(XMLApi.getNamedAttribute(layerIDNode, "name"));
                        for (Node groupIDNode : XMLApi.getAllChildren(layerIDNode)) {
                            int groupID = Integer.parseInt(XMLApi.getNamedAttribute(groupIDNode, "name"));
                            for (Node idNode : XMLApi.getAllChildren(groupIDNode)) {
                                int fhId = Integer.parseInt(XMLApi.getNamedAttribute(idNode, "name"));
                                Foothold fh = new Foothold(fhId, layerID, groupID);
                                for (Node n : XMLApi.getAllChildren(idNode)) {
                                    String name = XMLApi.getNamedAttribute(n, "name");
                                    String value = XMLApi.getNamedAttribute(n, "value");
                                    switch (name) {
                                        case "x1":
                                            fh.setX1(Integer.parseInt(value));
                                            break;
                                        case "y1":
                                            fh.setY1(Integer.parseInt(value));
                                            break;
                                        case "x2":
                                            fh.setX2(Integer.parseInt(value));
                                            break;
                                        case "y2":
                                            fh.setY2(Integer.parseInt(value));
                                            break;
                                        case "next":
                                            fh.setNext(Integer.parseInt(value));
                                            break;
                                        case "prev":
                                            fh.setPrev(Integer.parseInt(value));
                                            break;
                                        case "force":
                                            fh.setForce(Integer.parseInt(value));
                                            break;
                                        case "forbidFallDown":
                                            fh.setForbidFallDown(Integer.parseInt(value) != 0);
                                            break;
                                    }
                                }
                                field.addFoothold(fh);
                            }
                        }
                    }
                }
                Node portalNode = XMLApi.getFirstChildByNameBF(node, "portal");
                if (portalNode != null) {
                    for (Node idNode : XMLApi.getAllChildren(portalNode)) {
                        int portalId = Integer.parseInt(XMLApi.getNamedAttribute(idNode, "name"));
                        Portal portal = new Portal(portalId);
                        for (Node n : XMLApi.getAllChildren(idNode)) {
                            String name = XMLApi.getNamedAttribute(n, "name");
                            String value = XMLApi.getNamedAttribute(n, "value");
                            switch (name) {
                                case "pt":
                                    portal.setType(PortalType.getTypeByInt(Integer.parseInt(value)));
                                    break;
                                case "pn":
                                    portal.setName(value);
                                    break;
                                case "tm":
                                    portal.setTargetMapId(Integer.parseInt(value));
                                    break;
                                case "tn":
                                    portal.setTargetPortalName(value);
                                    break;
                                case "x":
                                    portal.setX(Integer.parseInt(value));
                                    break;
                                case "y":
                                    portal.setY(Integer.parseInt(value));
                                    break;
                                case "horizontalImpact":
                                    portal.setHorizontalImpact(Integer.parseInt(value));
                                    break;
                                case "verticalImpact":
                                    portal.setVerticalImpact(Integer.parseInt(value));
                                    break;
                                case "script":
                                    portal.setScript(value);
                                    break;
                                case "onlyOnce":
                                    portal.setOnlyOnce(Integer.parseInt(value) != 0);
                                    break;
                                case "delay":
                                    portal.setDelay(Integer.parseInt(value));
                                    break;
                            }
                        }
                        field.addPortal(portal);
                    }
                }
                Node lifeNode = XMLApi.getFirstChildByNameBF(node, "life");
                if (lifeNode != null) {
                    List<Node> idNodes = new ArrayList<>();
                    if (XMLApi.getFirstChildByNameBF(lifeNode, "isCategory") != null) {
                        for (Node catNode : XMLApi.getAllChildren(lifeNode)) {
                            if (!XMLApi.getNamedAttribute(catNode, "name").equals("isCategory")) {
                                idNodes.addAll(XMLApi.getAllChildren(catNode));
                            }
                        }
                    } else {
                        idNodes = XMLApi.getAllChildren(lifeNode);
                    }
                    for (Node idNode : idNodes) {
                        Life life = new Life(0);
                        for (Node n : XMLApi.getAllChildren(idNode)) {
                            String name = XMLApi.getNamedAttribute(n, "name");
                            String value = XMLApi.getNamedAttribute(n, "value");
                            switch (name) {
                                case "type":
                                    life.setLifeType(value);
                                    break;
                                case "id":
                                    life.setTemplateId(Integer.parseInt(value));
                                    break;
                                case "x":
                                    life.setX(Integer.parseInt(value));
                                    break;
                                case "y":
                                    life.setY(Integer.parseInt(value));
                                    break;
                                case "mobTime":
                                    life.setMobTime(Integer.parseInt(value));
                                    break;
                                case "f":
                                    life.setFlip(Integer.parseInt(value));
                                    break;
                                case "hide":
                                    life.setHide(Integer.parseInt(value) != 0);
                                    break;
                                case "fh":
                                    life.setFh(Integer.parseInt(value));
                                    break;
                                case "cy":
                                    life.setCy(Integer.parseInt(value));
                                    break;
                                case "rx0":
                                    life.setRx0(Integer.parseInt(value));
                                    break;
                                case "rx1":
                                    life.setRx1(Integer.parseInt(value));
                                    break;
                                case "limitedname":
                                    life.setLimitedName(value);
                                    break;
                                case "useDay":
                                    life.setUseDay(Integer.parseInt(value) != 0);
                                    break;
                                case "useNight":
                                    life.setUseNight(Integer.parseInt(value) != 0);
                                    break;
                                case "hold":
                                    life.setHold(Integer.parseInt(value) != 0);
                                    break;
                                case "nofoothold":
                                    life.setNoFoothold(Integer.parseInt(value) != 0);
                                    break;
                                case "dummy":
                                    life.setDummy(Integer.parseInt(value) != 0);
                                    break;
                                case "spine":
                                    life.setSpine(Integer.parseInt(value) != 0);
                                    break;
                                case "mobTimeOnDie":
                                    life.setMobTimeOnDie(Integer.parseInt(value) != 0);
                                    break;
                                case "regenStart":
                                    life.setRegenStart(Integer.parseInt(value));
                                    break;
                                case "mobAliveReq":
                                    life.setMobAliveReq(Integer.parseInt(value));
                                    break;
                                default:
                                    log.warn("unknown life property " + name + " with value " + value);
                                    break;
                            }
                        }
                        field.addLife(life);
                    }
                }
                Node reactorNode = XMLApi.getFirstChildByNameBF(node, "reactor");
                if (reactorNode != null) {
                    for (Node reactorIdNode : XMLApi.getAllChildren(reactorNode)) {
                        Reactor reactor = new Reactor(0);
                        for (Node valNode : XMLApi.getAllChildren(reactorIdNode)) {
                            String name = XMLApi.getNamedAttribute(valNode, "name");
                            String value = XMLApi.getNamedAttribute(valNode, "value");
                            int iVal = Util.isNumber(value) ? Integer.parseInt(value) : 0;
                            switch (name) {
                                case "id":
                                    reactor.setTemplateId(iVal);
                                    break;
                                case "x":
                                    Position curPos = reactor.getHomePosition();
                                    if (curPos == null) {
                                        curPos = new Position();
                                    }
                                    curPos.setX(iVal);
                                    reactor.setHomePosition(curPos);
                                    break;
                                case "y":
                                    curPos = reactor.getHomePosition();
                                    if (curPos == null) {
                                        curPos = new Position();
                                    }
                                    curPos.setY(iVal);
                                    reactor.setHomePosition(curPos);
                                    break;
                                case "reactorTime":
                                    reactor.setReactorTime(iVal);
                                    break;
                                case "f":
                                    reactor.setFlip(iVal);
                                    break;
                                case "name":
                                    reactor.setName(value);
                                    break;
                                case "phantomForest":
                                    reactor.setPhantomForest(iVal != 0);
                                    break;
                                default:
                                    log.warn(String.format("Unknown reactor property %s with value %s", name, value));
                            }
                        }
                        field.addReactor(reactor);
                    }
                }
                getFields().add(field);
            }
        }
    }

    public static List<Field> getFields() {
        return fields;
    }

    public static Field getFieldById(int id) {
        for (Field f : getFields()) {
            if (f.getId() == id) {
                return f;
            }
        }
        return getFieldFromFile(id);
    }

    private static Field getFieldFromFile(int id) {
        String fieldDir = ServerConstants.DAT_DIR + "/fields/" + id + ".dat";
        File file = new File(fieldDir);
        if (!file.exists()) {
            log.error("Could not find a field with id " + id);
            return null;
        } else {
            return readFieldFromFile(file);
        }
    }

    private static Field readFieldFromFile(File file) {
        Field field = null;
        try(DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file))) {
            field = new Field(dataInputStream.readInt(), -1);
            field.setTown(dataInputStream.readBoolean());
            field.setSwim(dataInputStream.readBoolean());
            field.setReturnMap(dataInputStream.readInt());
            field.setForcedReturn(dataInputStream.readInt());
            field.setMobRate(dataInputStream.readDouble());
            field.setFly(dataInputStream.readBoolean());
            field.setOnFirstUserEnter(dataInputStream.readUTF());
            field.setOnUserEnter(dataInputStream.readUTF());
            field.setFieldScript(dataInputStream.readUTF());
            field.setReactorShuffle(dataInputStream.readBoolean());
            field.setExpeditionOnly(dataInputStream.readBoolean());
            field.setPartyOnly(dataInputStream.readBoolean());
            field.setNeedSkillForFly(dataInputStream.readBoolean());
            field.setFixedMobCapacity(dataInputStream.readInt());
            field.setCreateMobInterval(dataInputStream.readInt());
            field.setTimeOut(dataInputStream.readInt());
            field.setTimeLimit(dataInputStream.readInt());
            field.setLvLimit(dataInputStream.readInt());
            field.setLvForceMove(dataInputStream.readInt());
            field.setConsumeItemCoolTime(dataInputStream.readInt());
            field.setLink(dataInputStream.readInt());
            field.setBossMobID(dataInputStream.readInt());
            field.setVrTop(dataInputStream.readInt());
            field.setVrLeft(dataInputStream.readInt());
            field.setVrBottom(dataInputStream.readInt());
            field.setVrRight(dataInputStream.readInt());
            short fhSize = dataInputStream.readShort();
            for (int j = 0; j < fhSize; j++) {
                Foothold fh = new Foothold(
                        dataInputStream.readInt(), dataInputStream.readInt(), dataInputStream.readInt()
                );
                fh.setX1(dataInputStream.readInt());
                fh.setY1(dataInputStream.readInt());
                fh.setX2(dataInputStream.readInt());
                fh.setY2(dataInputStream.readInt());
                fh.setNext(dataInputStream.readInt());
                fh.setPrev(dataInputStream.readInt());
                fh.setForce(dataInputStream.readInt());
                fh.setForbidFallDown(dataInputStream.readBoolean());
                field.addFoothold(fh);
            }
            short portalSize = dataInputStream.readShort();
            for (int j = 0; j < portalSize; j++) {
                Portal p = new Portal(dataInputStream.readInt());
                p.setType(PortalType.getTypeByInt(dataInputStream.readInt()));
                p.setName(dataInputStream.readUTF());
                p.setTargetMapId(dataInputStream.readInt());
                p.setTargetPortalName(dataInputStream.readUTF());
                p.setX(dataInputStream.readInt());
                p.setY(dataInputStream.readInt());
                p.setHorizontalImpact(dataInputStream.readInt());
                p.setVerticalImpact(dataInputStream.readInt());
                p.setScript(dataInputStream.readUTF());
                p.setOnlyOnce(dataInputStream.readBoolean());
                p.setDelay(dataInputStream.readInt());
                field.addPortal(p);
            }
            short lifeSize = dataInputStream.readShort();
            for (int j = 0; j < lifeSize; j++) {
                Life l = new Life(0);
                l.setLifeType(dataInputStream.readUTF());
                l.setTemplateId(dataInputStream.readInt());
                l.setX(dataInputStream.readInt());
                l.setY(dataInputStream.readInt());
                l.setHomePosition(new Position(l.getX(), l.getY()));
                l.setMobTime(dataInputStream.readInt());
                l.setFlip(dataInputStream.readInt());
                l.setHide(dataInputStream.readBoolean());
                l.setFh(dataInputStream.readInt());
                l.setCy(dataInputStream.readInt());
                l.setRx0(dataInputStream.readInt());
                l.setRx1(dataInputStream.readInt());
                l.setLimitedName(dataInputStream.readUTF());
                l.setUseDay(dataInputStream.readBoolean());
                l.setUseNight(dataInputStream.readBoolean());
                l.setHold(dataInputStream.readBoolean());
                l.setNoFoothold(dataInputStream.readBoolean());
                l.setDummy(dataInputStream.readBoolean());
                l.setSpine(dataInputStream.readBoolean());
                l.setMobTimeOnDie(dataInputStream.readBoolean());
                l.setRegenStart(dataInputStream.readInt());
                l.setMobAliveReq(dataInputStream.readInt());
                if ("m".equalsIgnoreCase(l.getLifeType())) {
                    MobGen mobGen = l.createMobGenFromLife();
                    field.addLife(mobGen);
                } else if ("n".equalsIgnoreCase(l.getLifeType())) {
                    Npc npc = l.createNpcFromLife();
                    field.addLife(npc);
                } else {
                    field.addLife(l);
                }
            }
            short reactSize = dataInputStream.readShort();
            for (int i = 0; i < reactSize; i++) {
                Reactor r = new Reactor(dataInputStream.readInt());
                r.setHomePosition(new Position(dataInputStream.readShort(), dataInputStream.readShort()));
                r.setReactorTime(dataInputStream.readInt());
                r.setFlip(dataInputStream.readInt());
                r.setName(dataInputStream.readUTF());
                r.setPhantomForest(dataInputStream.readBoolean());
                field.addReactor(r);
            }
            getFields().add(field);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return field;
    }

    public static void generateDatFiles() {
        log.info("Started generating field data.");
        long start = System.currentTimeMillis();
        loadFieldInfoFromWz();
        saveFields(ServerConstants.DAT_DIR + "/fields");
        log.info(String.format("Completed generating field data in %dms.", System.currentTimeMillis() - start));
    }

    public static Field getFieldCopyById(int id) {
        Field field = getFieldById(id);
        if (field == null) {
            return null;
        }
        Field copy = new Field(id, -1);
        copy.setTown(field.isTown());
        copy.setSwim(field.isSwim());
        copy.setReturnMap(field.getReturnMap());
        copy.setForcedReturn(field.getForcedReturn());
        copy.setMobRate(field.getMobRate());
        copy.setFly(field.isFly());
        copy.setOnFirstUserEnter(field.getOnFirstUserEnter());
        copy.setOnUserEnter(field.getOnUserEnter());
        copy.setFieldScript(field.getFieldScript());
        copy.setReactorShuffle(field.isReactorShuffle());
        copy.setExpeditionOnly(field.isExpeditionOnly());
        copy.setPartyOnly(field.isPartyOnly());
        copy.setNeedSkillForFly(field.isNeedSkillForFly());
        if (field.getFixedMobCapacity() != 0) {
            copy.setFixedMobCapacity(field.getFixedMobCapacity());
        }
        copy.setCreateMobInterval(field.getCreateMobInterval());
        copy.setTimeOut(field.getTimeOut());
        copy.setTimeLimit(field.getTimeLimit());
        copy.setLvLimit(field.getLvLimit());
        copy.setLvForceMove(field.getLvForceMove());
        copy.setConsumeItemCoolTime(field.getConsumeItemCoolTime());
        copy.setLink(field.getLink());
        for (Foothold fh : field.getFootholds()) {
            copy.addFoothold(fh.deepCopy());
        }
        for (Portal p : field.getPortals()) {
            copy.addPortal(p.deepCopy());
        }
        for (Life l : field.getLifes()) {
            copy.addLife(l.deepCopy());
        }
        for (Reactor r : field.getReactors()) {
            copy.addReactor(r);
        }
        copy.setObjectIDCounter(field.getNewObjectID());
        copy.setRuneStone(new RuneStone().getRandomRuneStone(copy));
        copy.setVrTop(field.getVrTop());
        copy.setVrLeft(field.getVrLeft());
        copy.setVrBottom(field.getVrBottom());
        copy.setVrRight(field.getVrRight());
        copy.startBurningFieldTimer();
        copy.generateMobs();
        return copy;
    }

    public static void clear() {
        getFields().clear();
    }
}
