package me.falsehonesty.asmhelper.dsl.instructions

import me.falsehonesty.asmhelper.AsmHelper
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode

enum class FieldAction(val opcode: Int) {
    GET_STATIC(Opcodes.GETSTATIC),
    PUT_STATIC(Opcodes.PUTSTATIC),
    GET_FIELD(Opcodes.GETFIELD),
    PUT_FIELD(Opcodes.PUTFIELD)
}

fun InsnListBuilder.getStatic(owner: String, name: String, desc: String) {
    field(FieldAction.GET_STATIC, owner, name, desc)
}

fun InsnListBuilder.getField(owner: String, name: String, desc: String) {
    field(FieldAction.GET_FIELD, owner, name, desc)
}

fun InsnListBuilder.putStatic(owner: String, name: String, desc: String) {
    field(FieldAction.PUT_STATIC, owner, name, desc)
}

fun InsnListBuilder.putField(owner: String, name: String, desc: String) {
    field(FieldAction.PUT_FIELD, owner, name, desc)
}

fun InsnListBuilder.field(action: FieldAction, descriptor: Descriptor) = this.field(action, descriptor.owner, descriptor.name, descriptor.desc)

fun InsnListBuilder.field(action: FieldAction, owner: String, name: String, desc: String) {
    val realName = AsmHelper.remapper.mapFieldAccess(name)

    insnList.add(FieldInsnNode(
        action.opcode,
        owner,
        realName,
        desc
    ))
}

fun InsnListBuilder.getLocalField(descriptor: Descriptor) {
    aload(0)
    field(FieldAction.GET_FIELD, descriptor)
}

fun InsnListBuilder.updateLocalField(descriptor: Descriptor, updater: InsnListBuilder.() -> Unit) {
    aload(0)
    getLocalField(descriptor)

    this.updater()

    field(FieldAction.PUT_FIELD, descriptor)
}

fun InsnListBuilder.setLocalField(descriptor: Descriptor, newValue: InsnListBuilder.() -> Unit) {
    aload(0)

    this.newValue()

    field(FieldAction.PUT_FIELD, descriptor)
}

fun InsnListBuilder.getLocalField(owner: String, name: String, desc: String) {
    aload(0)
    field(FieldAction.GET_FIELD, owner, name, desc)
}

fun InsnListBuilder.updateLocalField(owner: String, name: String, desc: String, updater: InsnListBuilder.() -> Unit) {
    aload(0)
    getLocalField(owner, name, desc)

    this.updater()

    field(FieldAction.PUT_FIELD, owner, name, desc)
}

fun InsnListBuilder.setLocalField(owner: String, name: String, desc: String, newValue: InsnListBuilder.() -> Unit) {
    aload(0)

    this.newValue()

    field(FieldAction.PUT_FIELD, owner, name, desc)
}