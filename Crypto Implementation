extension FlashUtilities {
    static func encrypt(bin: Data, key: Data, initVector: Data) throws -> Data {
        let sealedBox = try AES.CBC.encrypt(bin, using: SymmetricKey(data: key), iv: initVector)
        return sealedBox.combined!
    }
}
