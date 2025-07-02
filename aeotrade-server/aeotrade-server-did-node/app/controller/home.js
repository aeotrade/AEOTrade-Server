const { Controller } = require('egg');
const VerificationMethodKeySuffix="#keys-"
const DidContext="https://www.w3.org/ns/did/v1"
class HomeController extends Controller {

  async didDoc() {
    const { ctx } = this;
    const params = ctx.request.body;
    let did=params.did
    let privateKey=params.key
    let PkPEM=params.pem

    console.log(params)
    const controller=[did]
    const keyId=did+VerificationMethodKeySuffix+1
    const authentication=[keyId]
    // await ctx.helper.private2Pubkey(privateKey)
    // return
    const v=await ctx.helper.newVerificationMethod(keyId,did,PkPEM)
    let created=new Date()
    let doc={
      '@context':DidContext,
      id:did,
      created:created,
      updated:created,
      verificationMethod:[v],
      authentication:authentication,
      controller:controller,
    }


    let signStr=await ctx.helper.signWithPrivateKey(privateKey,doc)

    let proof={
      proofValue:signStr,
      created:created.toISOString(),
      proofPurpose:'assertionMethod',
      type: "ECDSA-SHA256",
      verificationMethod:doc.authentication[0]
    }

    doc.proof=proof;
    ctx.response.body =JSON.stringify(doc)
  }
  async issue(){
    const { ctx } = this;
    let params = ctx.request.body;
    let res=await ctx.helper.createVc(params)
    ctx.response.body=res;
  }
  async decode(){
    const { ctx } = this;
    let params = ctx.request.body;
    console.log(params)
    let res=await ctx.helper.decodeABI(params.method,Buffer.from(params.decodestr, 'base64'));
    console.log(res)
    console.log(res.toString())
    ctx.response.body=res.toString();
  }
}

module.exports = HomeController;
