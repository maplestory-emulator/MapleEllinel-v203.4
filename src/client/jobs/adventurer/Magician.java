package client.jobs.adventurer;

import client.Client;
import client.character.Char;
import client.character.HitInfo;
import client.character.skills.*;
import client.field.Field;
import client.jobs.Job;
import client.life.AffectedArea;
import client.life.Mob;
import client.life.MobTemporaryStat;
import client.life.Summon;
import connection.InPacket;
import constants.JobConstants;
import enums.ChatMsgColour;
import enums.MobStat;
import enums.Stat;
import loaders.SkillData;
import packet.CField;
import packet.WvsContext;
import util.Position;
import util.Util;

import java.util.Arrays;

import static client.character.skills.CharacterTemporaryStat.*;
import static client.character.skills.SkillStat.*;

/**
 * Created on 12/14/2017.
 */
public class Magician extends Job {
    public static final int MAGIC_GUARD = 2001002;
    public static final int MP_EATER_FP = 2100000;
    public static final int POISON_BREATH = 2101005;
    public static final int MAGIC_BOOSTER_FP = 2101008;
    public static final int MEDITATION_FP = 2101001;
    public static final int IGNITE = 2101010;
    public static final int BURNING_MAGIC = 2110000;
    public static final int POISON_MIST = 2111003;
    public static final int TELEPORT_MASTERY_FP = 2111007;
    public static final int ELEMENTAL_DECREASE = 2111008;
    public static final int VIRAL_SLIME = 2111010;
    public static final int PARALYZE = 2121006;
    public static final int MIST_ERUPTION = 2121003;
    public static final int FLAME_HAZE = 2121011;
    public static final int INFINITY = 2121004;
    public static final int IFRIT = 2121005;
    public static final int MAPLE_WARRIOR_FP = 2121000;


    private final int[] buffs = new int[]{
            MAGIC_GUARD,
            IGNITE,
            MAGIC_BOOSTER_FP,
            MEDITATION_FP,
            TELEPORT_MASTERY_FP,
            ELEMENTAL_DECREASE,
            INFINITY,
            IFRIT,
            MAPLE_WARRIOR_FP,
            MEDITATION_FP,
    };

    public Magician(Char chr) {
        super(chr);
    }

    @Override
    public void handleAttack(Client c, AttackInfo attackInfo) {
        Char chr = c.getChr();
        TemporaryStatManager tsm = chr.getTemporaryStatManager();
        Skill skill = chr.getSkill(attackInfo.skillId);
        int skillID = 0;
        SkillInfo si = null;
        boolean hasHitMobs = attackInfo.mobAttackInfo.size() > 0;
        int slv = 0;
        if (skill != null) {
            si = SkillData.getSkillInfoById(skill.getSkillId());
            slv = skill.getCurrentLevel();
            skillID = skill.getSkillId();
        }
        handleIgnite(attackInfo, chr, tsm, slv);
        Option o1 = new Option();
        Option o2 = new Option();
        Option o3 = new Option();
        switch (skillID) {
            case POISON_BREATH:
                for(MobAttackInfo mai : attackInfo.mobAttackInfo) {
                    if(Util.succeedProp(si.getValue(prop, slv))) {
                        Mob mob = (Mob) chr.getField().getLifeByObjectID(mai.mobId);
                        MobTemporaryStat mts = mob.getTemporaryStat();
                        mts.createAndAddBurnedInfo(chr.getId(), skill, 1);
                    }
                }
                break;
            case POISON_MIST:
                AffectedArea aa = AffectedArea.getAffectedArea(attackInfo);
                aa.setMobOrigin((byte) 0);
                aa.setCharID(chr.getId());
                int x = attackInfo.forcedX;
                int y = attackInfo.forcedY;
                aa.setPosition(new Position(x, y));
                aa.setRect(aa.getPosition().getRectAround(si.getRects().get(0)));
                chr.getField().spawnAffectedArea(aa);
                break;
            case TELEPORT_MASTERY_FP:
                for(MobAttackInfo mai : attackInfo.mobAttackInfo) {
                    if(Util.succeedProp(si.getValue(prop, slv))) {
                        Mob mob = (Mob) chr.getField().getLifeByObjectID(mai.mobId);
                        MobTemporaryStat mts = mob.getTemporaryStat();
                        o1.nOption = 1;
                        o1.rOption = skill.getSkillId();
                        o1.tOption = si.getValue(time, slv);
                        mts.addStatOptions(MobStat.Stun, o1);
                        mts.createAndAddBurnedInfo(chr.getId(), skill, 1);
                    }
                }
                break;
            case FLAME_HAZE:
                for(MobAttackInfo mai : attackInfo.mobAttackInfo) {
                    if(Util.succeedProp(si.getValue(prop, slv))) {
                        Mob mob = (Mob) chr.getField().getLifeByObjectID(mai.mobId);
                        MobTemporaryStat mts = mob.getTemporaryStat();
                        o1.nOption = 1;
                        o1.rOption = skill.getSkillId();
                        o1.tOption = si.getValue(time, slv);
                        mts.addStatOptions(MobStat.Invincible, o1);
                        o1.nOption = 1;
                        o1.rOption = skill.getSkillId();
                        o1.tOption = si.getValue(time, slv);
                        mts.addStatOptions(MobStat.Speed, o1);
                        mts.createAndAddBurnedInfo(chr.getId(), skill, 1);
                    }
                }
                break;
            case IFRIT:
                for(MobAttackInfo mai : attackInfo.mobAttackInfo) {
                    Mob mob = (Mob) chr.getField().getLifeByObjectID(mai.mobId);
                    MobTemporaryStat mts = mob.getTemporaryStat();
                    mts.createAndAddBurnedInfo(chr.getId(), skill, 1);
                }
                break;
        }

    }

    private void handleIgnite(AttackInfo attackInfo, Char chr, TemporaryStatManager tsm, int slv) {
        if(tsm.hasStat(WizardIgnite)) {
            SkillInfo igniteInfo = getInfo(IGNITE);
            for(MobAttackInfo mai : attackInfo.mobAttackInfo) {
                if (Util.succeedProp(igniteInfo.getValue(prop, slv))) {
                    Mob mob = (Mob) chr.getField().getLifeByObjectID(mai.mobId);
                    AffectedArea aa = AffectedArea.getAffectedArea(attackInfo);
                    aa.setMobOrigin((byte) 1);
                    aa.setCharID(chr.getId());
                    aa.setPosition(mob.getPosition());
                    aa.setRect(aa.getPosition().getRectAround(igniteInfo.getRects().get(0)));
                    chr.getField().spawnAffectedArea(aa);
                }
            }
        }
    }

    @Override
    public void handleSkill(Client c, int skillID, byte slv, InPacket inPacket) {
        Char chr = c.getChr();
        Skill skill = chr.getSkill(skillID);
        SkillInfo si = null;
        if(skill != null) {
            si = SkillData.getSkillInfoById(skillID);
        }
        chr.chatMessage(ChatMsgColour.YELLOW, "SkillID: " + skillID);
        if (isBuff(skillID)) {
            handleBuff(c, inPacket, skillID, slv);
        } else {
            switch(skillID) {
            }
        }
    }

    @Override
    public void handleHit(Client c, InPacket inPacket, HitInfo hitInfo) {

    }

    private boolean isBuff(int skillID) {
        return Arrays.stream(buffs).anyMatch(b -> b == skillID);
    }

    private void handleBuff(Client c, InPacket inPacket, int skillID, byte slv) {
        Char chr = c.getChr();
        SkillInfo si = SkillData.getSkillInfoById(skillID);
        TemporaryStatManager tsm = c.getChr().getTemporaryStatManager();
        Option o1 = new Option();
        Option o2 = new Option();
        Option o3 = new Option();
        switch (skillID) {
            case MAGIC_GUARD:
                o1.nOption = si.getValue(x, slv);
                o1.rOption = skillID;
                o1.tOption = 0;
                tsm.putCharacterStatValue(MagicGuard, o1);
                break;
            case MAGIC_BOOSTER_FP:
                o1.nOption = si.getValue(x, slv);
                o1.rOption = skillID;
                o1.tOption = si.getValue(time, slv);
//                o1.nValue = si.getValue(x, slv);
//                o1.nReason = skillID;
//                o1.tStart= (int) System.currentTimeMillis();
//                o1.tTerm = si.getValue(time, slv);
                tsm.putCharacterStatValue(Booster, o1);
                break;
            case MEDITATION_FP:
                o1.nValue = si.getValue(indieMad, slv);
                o1.nReason = skillID;
                o1.tStart = (int) System.currentTimeMillis();
                o1.tTerm = si.getValue(time, slv);
                tsm.putCharacterStatValue(IndieMAD, o1);
                break;
            case IGNITE:
                o1.nOption = 1;
                o1.rOption = skillID;
                o1.tOption = 0;
                tsm.putCharacterStatValue(WizardIgnite, o1);
                break;
            case ELEMENTAL_DECREASE:
                o1.nOption = si.getValue(x, slv);
                o1.rOption = skillID;
                o1.tOption = si.getValue(time, slv);
                tsm.putCharacterStatValue(ElementalReset, o1);
                break;
            case TELEPORT_MASTERY_FP:
                o1.nOption = 1;
                o1.rOption = skillID;
                o1.tOption = 0;
                tsm.putCharacterStatValue(TeleportMasteryOn, o1);
                break;
            case INFINITY:
                o1.nOption = si.getValue(damage, slv);
                o1.rOption = skillID;
                o1.tOption = si.getValue(time, slv);
                tsm.putCharacterStatValue(Infinity, o1);
                break;
            case IFRIT:
                Summon summon = Summon.getSummonBy(c.getChr(), skillID, slv);
                Field field = c.getChr().getField();
                field.addLife(summon);
                summon.setCharLevel((byte) chr.getStat(Stat.level));
                summon.setPosition(chr.getPosition().deepCopy());
                summon.setMoveAction((byte) 1);
                summon.setCurFoothold((short) field.findFootHoldBelow(summon.getPosition()).getId());
                summon.setMoveAbility((byte) 1);
                summon.setAssistType((byte) 1);
                summon.setEnterType((byte) 1);
                summon.setBeforeFirstAttack(false);
                summon.setTemplateId(skillID);
                summon.setAttackActive(true);
                c.write(CField.summonedCreated(chr.getId(), summon));
                break;
            case MAPLE_WARRIOR_FP:
                o1.nValue = si.getValue(x, slv);
                o1.nReason = skillID;
                o1.tStart = (int) System.currentTimeMillis();
                o1.tTerm = si.getValue(time, slv);
                tsm.putCharacterStatValue(IndieStatR, o1);
                break;
        }
        c.write(WvsContext.temporaryStatSet(tsm));
    }

    @Override
    public boolean isHandlerOfJob(short id) {
        JobConstants.JobEnum jobEnum = JobConstants.JobEnum.getJobById(id);
        switch(jobEnum) {
            case MAGICIAN:
            case FP_WIZARD:
            case FP_MAGE:
            case FP_ARCHMAGE:
            case IL_WIZARD:
            case IL_MAGE:
            case IL_ARCHMAGE:
            case CLERIC:
            case PRIEST:
            case BISHOP:
                return true;
            default:
                return false;
        }
    }
}
