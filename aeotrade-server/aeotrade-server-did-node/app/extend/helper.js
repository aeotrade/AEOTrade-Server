
const {handler,tool} = require('chainmaker-wallet-jssdk');
const jsrsasign = require('jsrsasign');
const rsu = require('jsrsasign-util');
const fs = require('fs');
const abi=require('ethers/abi')
const abiFile = './circle.json';
module.exports = {
    /**
     * Generate DID from public key
     */


    async newVerificationMethod(id, controller, pkPem) {
        try {
            const address = handler.pubKey2Address(pkPem);
            // console.log(address);
            // return address;
            let proof={
                id:id,
                controller:controller,
                publicKeyPem:pkPem.replace(/\n/g, '\n') + '\n',
                address:address,
                type:"ECDSA-SHA256"
            }
            return proof
        } catch (error) {
            console.error('Failed to import chainmaker-wallet-jssdk:', error);
            throw error;
        }
    },
    async  stringToHex(str) {
        const encoder = new TextEncoder();
        const bytes = encoder.encode(str);
        return Array.from(bytes)
            .map(byte => byte.toString(16).padStart(2, '0'))
            .join('');
    },
    async signWithPrivateKey(pem, jsonData) {
        const privateKeyObj = jsrsasign.KEYUTIL.getKey(pem);
        const signature = new jsrsasign.Signature({
            alg: 'SHA256withECDSA',
        });
        const str = JSON.stringify(jsonData);
        const encoder = new TextEncoder();
        const bytes = encoder.encode(str);
        const hexContent = Array.from(bytes)
            .map(byte => byte.toString(16).padStart(2, '0'))
            .join('');
        signature.init(privateKeyObj);
        signature.updateHex(hexContent);
        const res = signature.sign();
        let base64=jsrsasign.hex2b64(res)
        return base64;

    },
    async createVc(params){
        let vc={
            '@context':[
                "https://www.w3.org/2018/credentials/v1",
                "https://www.w3.org/2018/credentials/examples/v1"
            ],
            'id':params.id,
            'type':['VerifiableCredential'],
            'credentialSubject':params.subject,
            'issuer':params.issuer,
            'issuanceDate': await this.isoTimeString(new Date()),
            'expirationDate': await  this.isoTimeString(params.expirationDate),
            'template':{
                "id": params.vcTemplateId,
                "name": params.vcTempName
            }
        }
        let signContent=await this.signWithPrivateKey(params.key,vc)
        let proof={
            proofValue:signContent,
            created:await this.isoTimeString(new Date()),
            proofPurpose:'assertionMethod',
            type: "ECDSA-SHA256",
            verificationMethod:params.issuer+'#keys-1'
        }
        vc.proof=proof;
        return JSON.stringify(vc)
    },
    async isoTimeString(time){
        // // 1. 获取当前时间（本地时区）
        // const now = new Date(time);
        // const timezoneOffset = -now.getTimezoneOffset() / 60; // 中国时区返回 +8
        // const isoString = now.toISOString()
        //     .replace('Z', `${timezoneOffset >= 0 ? '+' : '-'}${Math.abs(timezoneOffset).toString().padStart(2, '0')}:00`);
        //
        // console.log(isoString); // 输出如 "2024-03-08T16:49:03+08:00"

        const formatOptions = {
            timeZone: 'Asia/Shanghai', // 指定时区（如中国时区）
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false, // 24小时制
            timeZoneName: 'longOffset' // 显示时区偏移
        };

        const formatter = new Intl.DateTimeFormat('en-CA', formatOptions);
        console.log(formatter);
        console.log(new Date(time))
        let formatted = formatter.format(new Date(time));

        // 处理格式（兼容不同浏览器）
        formatted = formatted
            .replace(/, /g, 'T')
            .replace(/(\d{2}:\d{2}:\d{2}) (.+)/, '$1$2')
            .replace('GMT', '');

        return formatted;
    },
    async decodeABI(functionName,hexData){
        const interface=new abi.Interface(require(abiFile));
        return interface.decodeFunctionResult(functionName,hexData)
    },
    async convertBigInt(value) {
        if (typeof value === 'bigint') {
            return value.toString(); // 保留精度
        } else if (Array.isArray(value)) {
            return value.map(this.convertBigInt);
        } else if (value && typeof value === 'object') {
            return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, this.convertBigInt(v)]));
        } else {
            return value;
        }
    }
}
