package net.dain.hongozmod.colony;

import net.dain.hongozmod.colony.role.*;
import net.dain.hongozmod.util.RandomSelection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class Colony<
        MemberType extends ColonyMember,
        QueenType extends ColonyQueen>{

    public static final int WORKER_BASE_CHANCE = 160;
    public static final int EXPLORER_BASE_CHANCE = 80;
    public static final int WARRIOR_BASE_CHANCE = 40;
    public static final int ROYAL_WARRIOR_BASE_CHANCE = 10;

    public static final int HEIR_BASE_CHANCE = 1;
    public static final int FOOD_STORE_BASE_CHANCE = 20;
    public static final float REJECT_BASE_CHANCE = 0.5f;

    protected RandomSelection<ColonyRoles> randomRole;

    public static final int DEFAULT_CAPACITY = 50;
    public static final int MIN_FAIL_GROW_ATTEMPTS = 10;
    public final int maxCapacity;
    protected int actualSize = 0;
    protected int growAttempts = 0;
    protected int colonyAge = 0;

    protected List<ColonyMember> newborns = new ArrayList<>(10);
    protected List<ColonyWorker> workers = new ArrayList<>(10);
    protected List<ColonyExplorer> explorers = new ArrayList<>(10);
    protected List<ColonyWarrior> warriors = new ArrayList<>(10);
    protected List<ColonyRoyalWarrior> royalWarriors = new ArrayList<>(5);
    protected List<ColonyMember> heirs = new ArrayList<>(3);
    protected QueenType ColonyQueen;

    protected final RandomSource random = RandomSource.create();

    protected LivingEntity target = null;
    protected AlertLevel alertLevel = AlertLevel.NONE;

    /*-------------------------------- Methods start --------------------------------*/

    public Colony(QueenType newQueen, int capacity){
        this.ColonyQueen = newQueen;
        this.maxCapacity = capacity;
    }
    public Colony(QueenType newQueen){
        this.ColonyQueen = newQueen;
        this.maxCapacity = DEFAULT_CAPACITY;
    }

    public boolean requestColonyGrow(ColonyMember member){
        return this.getQueen() == member && this.growColony();
    }
    protected boolean growColony(){
        this.getQueen().setColony(this.growColony(this.getClass()));
        return true;
    }
    protected <ColonyType extends Colony<MemberType, QueenType>>
    ColonyType growColony(Class<ColonyType> colonyType){
        ColonyType newColony = colonyType.cast(new Colony<>(
                this.getQueen(),
                this.maxCapacity + DEFAULT_CAPACITY));

        newColony.setMembers(this);
        newColony.setHeirs(this.getHeirs());
        newColony.colonyAge = this.colonyAge;
        newColony.actualSize = this.actualSize;
        newColony.setTarget(this.getTarget());

        return newColony;
    }

    public List<ColonyMember> getMembers(){
        List<ColonyMember> members = new ArrayList<>();
        members.addAll(this.getWorkers());
        members.addAll(this.getExplorers());
        members.addAll(this.getWarriors());
        members.addAll(this.getRoyalWarriors());

        return members;
    }
    public List<ColonyMember> getAllMembers(){
        List<ColonyMember> members = new ArrayList<>();
        members.addAll(this.getNewborns());
        members.addAll(this.getMembers());
        members.add(this.getQueen());

        return members;
    }

    public List<ColonyMember> getNewborns(){
        return this.newborns;
    }
    public List<ColonyWorker> getWorkers(){
        return this.workers;
    }
    public List<ColonyExplorer> getExplorers(){
        return this.explorers;
    }
    public List<ColonyWarrior> getWarriors(){
        return this.warriors;
    }
    public List<ColonyRoyalWarrior> getRoyalWarriors(){
        return this.royalWarriors;
    }
    public List<ColonyMember> getHeirs(){
        return this.heirs;
    }

    public void setMembers(@Nullable List<ColonyMember> newborns,
                           @Nullable List<ColonyWorker> workers,
                           @Nullable List<ColonyExplorer> explorers,
                           @Nullable List<ColonyWarrior> warriors,
                           @Nullable List<ColonyRoyalWarrior> royalWarriors){

        this.newborns = newborns != null? newborns : this.newborns;
        this.workers = workers != null? workers : this.workers;
        this.explorers = explorers != null? explorers : this.explorers;
        this.warriors = warriors != null? warriors : this.warriors;
        this.royalWarriors = royalWarriors != null? royalWarriors : this.royalWarriors;
    }

    public void setMembers(@NotNull Colony<MemberType, QueenType> colony){
        this.setMembers(
                colony.getNewborns(),
                colony.getWorkers(),
                colony.getExplorers(),
                colony.getWarriors(),
                colony.getRoyalWarriors());
    }

    public EnumSet<ColonyRoles> allowedRolesBecomeHeir(){
        // This is stupid :/
        return EnumSet.of(ColonyRoles.HEIR, ColonyRoles.WORKER);
    }
    public boolean canBecomeHair(@NotNull EnumSet<ColonyRoles> roles){
        // Java enums are shit >:(

        int flags = 0b0, allowed = 0b0;
        for (ColonyRoles role : roles) {
            flags |= role.value;
        }
        for(ColonyRoles role : this.allowedRolesBecomeHeir()){
            allowed |= role.value;
        }

        return (flags & allowed) != 0;
    }


    public void setHeirs(List<ColonyMember> heirs){
        List<ColonyMember> newHeirs = new ArrayList<>();
        heirs.forEach(possibleHeir -> {
            if(this.canBecomeHair(possibleHeir.getRoles())){
                newHeirs.add(possibleHeir);
                possibleHeir.addRole(ColonyRoles.HEIR);
            }
        });
        this.heirs.clear();
        this.heirs = newHeirs;
    }
    public void addHeir(MemberType newHeir){
        if(this.canBecomeHair(newHeir.getRoles())) {
            this.heirs.add(newHeir);
        }
    }
    public void addHeirs(List<ColonyMember> newHeirs){
        List<ColonyMember> accepted = new ArrayList<>();
        newHeirs.forEach(possibleHeir -> {
            if(this.canBecomeHair(possibleHeir.getRoles())){
                accepted.add(possibleHeir);
            }
        });
        this.heirs.addAll(accepted);
    }

    public @NotNull QueenType getQueen(){
        return this.ColonyQueen;
    }
    public boolean requestAdoption(@NotNull MemberType outsider){
        this.alertLevel = AlertLevel.MEDIUM;
        return this.addNewMember(outsider, true);
    }
    public boolean addChild(@NotNull MemberType newChild){
        return this.addNewMember(newChild, false);
    }
    public boolean addNewMember(@NotNull ColonyMember newMember, boolean outsider){
        if( (!outsider || this.approveMember(newMember)) &&
            this.assignRole(newMember) && newMember.setColony(this)){

            newMember.returnToQueen(AlertLevel.HIGH);
            return true;
        }

        return false;
    }
    protected boolean approveMember(@NotNull ColonyMember newMember){
        return (this.random.nextFloat() > this.getRejectChance()) && newMember.getColony() == null;
    }
    protected boolean assignRole(@NotNull ColonyMember member){
        final int workerChance = WORKER_BASE_CHANCE + this.getQueen().getWorkerChance();
        final int explorerChance = EXPLORER_BASE_CHANCE + this.getQueen().getExplorerChance();
        final int warriorChance = WARRIOR_BASE_CHANCE + this.getQueen().getWarriorChance();
        final int royalWarriorChance = ROYAL_WARRIOR_BASE_CHANCE + this.getQueen().getRoyalWarriorChance();
        final int totalChance = workerChance + explorerChance + warriorChance + royalWarriorChance;

        ColonyRoles newRole = (new RandomSelection<ColonyRoles>())
                .add(ColonyRoles.WORKER, workerChance)
                .add(ColonyRoles.EXPLORER, explorerChance)
                .add(ColonyRoles.WARRIOR, warriorChance)
                .add(ColonyRoles.ROYAL_WARRIOR, royalWarriorChance)
                .next();

        if(newRole.equals(ColonyRoles.WORKER)){
            final int heirChance = HEIR_BASE_CHANCE + this.getQueen().getHeirChance();
            final int foodStoreChance = FOOD_STORE_BASE_CHANCE + this.getQueen().getFoodStoreChance();

            newRole = (new RandomSelection<ColonyRoles>())
                    .add(ColonyRoles.WORKER, workerChance)
                    .add(ColonyRoles.FOOD_SACK, foodStoreChance)
                    .add(ColonyRoles.HEIR, heirChance)
                    .next();
        }

        if(this.assignRole(member, newRole)){
            return true;
        }
        return false;
    }
    protected boolean assignRole(@NotNull ColonyMember member, ColonyRoles role){
        MemberType m = member.changeRole(role);

        return m != null;
    }

    public float getRejectChance(){
        return (float)this.random.triangle(0.0f, REJECT_BASE_CHANCE) +
                this.getQueen().getRejectChance();
    }

    public int size(){
        return this.workers.size() + this.explorers.size() + this.warriors.size() + this.royalWarriors.size();
    }
    public int completeSize() {
        return this.size() + this.getNewborns().size() + 1;
    }

    public void queenDied(){
        this.alertLevel = AlertLevel.CRITICAL;
    }
    protected QueenType selectNewQueen(){
        List<ColonyMember> heirs = (List<ColonyMember>) this.getHeirs();

        heirs.sort(Comparator.comparingInt(ColonyMember::queeningScore));
        QueenType newQueen;

        for (ColonyMember heir : heirs) {
            if(heir.canBecomeQueen()){
                newQueen = (QueenType) heir.becomeQueen();
                return newQueen;
            }
        }
        return null;
    }

    public void setTarget(LivingEntity target){
        if(this.alertLevel.ordinal() < AlertLevel.MEDIUM.ordinal()){
            this.alertLevel = AlertLevel.MEDIUM;
        }
        this.alertLevel = AlertLevel.values()[this.alertLevel.ordinal() + 1];
        this.target = target;
    }
    public LivingEntity getTarget() {
        return this.target;
    }
    public boolean inAlert(){
        return this.getAlertLevel() != AlertLevel.NONE || this.target != null;
    }
    public AlertLevel getAlertLevel(){
        return this.alertLevel;
    }
}
