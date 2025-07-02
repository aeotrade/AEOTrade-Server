/**
 * @param {Egg.Application} app - egg application
 */
module.exports = app => {
  const { router, controller } = app;
  router.post('/didDoc', controller.home.didDoc); //did文档
  router.post('/issue', controller.home.issue);//vc文档
  router.post('/decode', controller.home.decode);//解码
};
