package ls

data class Inode(val type: InodeType, val size: String, val permissions: String, val lastEditedTime: String)