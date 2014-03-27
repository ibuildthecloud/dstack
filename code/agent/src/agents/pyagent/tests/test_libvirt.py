from .common_fixtures import *  # NOQA
from cattle import CONFIG_OVERRIDE
from cattle.progress import LogProgress
from .test_libvirt_storage import if_libvirt, fake_image
from .test_libvirt_storage import fake_pool, fake_volume
from .test_libvirt_storage import random_qcow2, random_qcow2_bz2  # NOQA
from .test_libvirt_storage import random_qcow2_gz, pool_dir  # NOQA
from cattle.plugins.libvirt import enabled


if enabled():
    import libvirt
    from cattle.plugins.libvirt_directory_pool import DirectoryPoolDriver
    from cattle.plugins.libvirt.utils import get_preferred_libvirt_type
    CONFIG_OVERRIDE['HOME'] = SCRATCH_DIR


def _delete_instance(name):
    conn = libvirt.open('qemu:///system')
    for c in conn.listAllDomains():
        if c.name() == name:
            c.destroy()


@if_libvirt
def test_image_activate(random_qcow2, pool_dir, agent, responses):
    def pre(req):
        req['data']['imageStoragePoolMap']['image'] = fake_image(random_qcow2)
        req['data']['imageStoragePoolMap']['storagePool'] = fake_pool(pool_dir)

    def post(req, resp):
        assert resp['data']['+data']['libvirt']['filename'].endswith('.qcow2')
        del resp['data']['+data']['libvirt']['filename']

        assert int(resp['data']['+data']['libvirt']['actual-size']) > 200000
        del resp['data']['+data']['libvirt']['actual-size']

    event_test(agent, 'libvirt/image_activate', pre_func=pre, post_func=post)


@if_libvirt
def test_image_activate_gz(random_qcow2_gz, pool_dir, agent, responses):
    def pre(req):
        req['data']['imageStoragePoolMap']['image'] = \
            fake_image(random_qcow2_gz)
        req['data']['imageStoragePoolMap']['storagePool'] = fake_pool(pool_dir)

    def post(req, resp):
        assert resp['data']['+data']['libvirt']['filename'].endswith('.qcow2')
        del resp['data']['+data']['libvirt']['filename']

        assert int(resp['data']['+data']['libvirt']['actual-size']) > 200000
        del resp['data']['+data']['libvirt']['actual-size']

    event_test(agent, 'libvirt/image_activate', pre_func=pre, post_func=post)


@if_libvirt
def test_image_activate_bz2(random_qcow2_bz2, pool_dir, agent, responses):
    def pre(req):
        req['data']['imageStoragePoolMap']['image'] = \
            fake_image(random_qcow2_bz2)
        req['data']['imageStoragePoolMap']['storagePool'] = fake_pool(pool_dir)

    def post(req, resp):
        assert resp['data']['+data']['libvirt']['filename'].endswith('.qcow2')
        del resp['data']['+data']['libvirt']['filename']

        assert int(resp['data']['+data']['libvirt']['actual-size']) > 200000
        del resp['data']['+data']['libvirt']['actual-size']

    event_test(agent, 'libvirt/image_activate', pre_func=pre, post_func=post)


@if_libvirt
def test_volume_activate(random_qcow2, pool_dir, agent, responses):
    volume = fake_volume(image_file=random_qcow2)
    image = volume.image
    pool = fake_pool(pool_dir)

    driver = DirectoryPoolDriver()
    driver.image_activate(image, pool, LogProgress())

    def pre(req):
        req['data']['volumeStoragePoolMap']['volume'] = volume
        req['data']['volumeStoragePoolMap']['storagePool'] = pool

    def post(req, resp):
        assert resp['data']['+data']['libvirt']['filename'].endswith('.qcow2')
        assert resp['data']['+data']['libvirt']['backing-filename']\
            .endswith('.qcow2')
        assert resp['data']['+data']['libvirt']['full-backing-filename']\
            .endswith('.qcow2')

        assert int(resp['data']['+data']['libvirt']['actual-size']) > 200000
        del resp['data']['+data']['libvirt']['actual-size']

        del resp['data']['+data']['libvirt']['filename']
        del resp['data']['+data']['libvirt']['backing-filename']
        del resp['data']['+data']['libvirt']['full-backing-filename']

    event_test(agent, 'libvirt/volume_activate', pre_func=pre, post_func=post)


@if_libvirt
def test_volume_deactivate(random_qcow2, pool_dir, agent, responses):
    volume = fake_volume(image_file=random_qcow2)
    pool = fake_pool(pool_dir)

    def pre(req):
        req['data']['volumeStoragePoolMap']['volume'] = volume
        req['data']['volumeStoragePoolMap']['storagePool'] = pool

    event_test(agent, 'libvirt/volume_deactivate', pre_func=pre)


@if_libvirt
def test_instance_activate(random_qcow2, pool_dir, agent, responses):
    _delete_instance('c861f990-4472-4fa1-960f-65171b544c28')

    volume = fake_volume(image_file=random_qcow2)
    image = volume.image
    pool = fake_pool(pool_dir)
    volume['storagePools'] = [pool]

    driver = DirectoryPoolDriver()
    driver.image_activate(image, pool, LogProgress())
    driver.volume_activate(volume, pool, LogProgress())

    def pre(req):
        req.data.instanceHostMap.instance.image = image
        req.data.instanceHostMap.instance.volumes.append(volume)

    def post(_, resp):
        assert resp['data']['instance']['+data']['+libvirt']['xml'] is not None
        resp['data']['instance']['+data']['+libvirt']['xml'] = '<xml/>'

    event_test(agent, 'libvirt/instance_activate', pre_func=pre,
               post_func=post)

    _delete_instance('c861f990-4472-4fa1-960f-65171b544c28')


@if_libvirt
def test_instance_deactivate(random_qcow2, pool_dir, agent, responses):
    CONFIG_OVERRIDE['STOP_TIMEOUT'] = 1

    test_instance_activate(random_qcow2, pool_dir, agent, responses)

    def post(req, resp):
        pass

    event_test(agent, 'libvirt/instance_deactivate', post_func=post)


@if_libvirt
def test_ping(agent, responses):
    CONFIG_OVERRIDE['DOCKER_ENABLED'] = 'false'
    CONFIG_OVERRIDE['HOSTNAME'] = 'localhost'
    CONFIG_OVERRIDE['LIBVIRT_UUID'] = 'testuuid'

    def post(req, resp):
        resp['data']['resources'] = filter(lambda x: x['kind'] == 'libvirt',
                                           resp['data']['resources'])
        assert resp['data']['resources'][1]['name'] == \
            resp['data']['resources'][0]['name'] + ' Storage Pool ' + \
            resp['data']['resources'][1]['data']['libvirt']['poolPath']

        resp['data']['resources'][1]['name'] = \
            resp['data']['resources'][0]['name'] + ' Storage Pool'

        resp['data']['resources'][1]['data']['libvirt']['poolPath'] = \
            'pool path'

        assert resp['data']['resources'][1]['uuid'].startswith(
            resp['data']['resources'][0]['uuid'] + '-')

        resp['data']['resources'][1]['uuid'] = 'testuuid-pool'

        assert resp['data']['resources'][0]['data']['libvirt']['type'] in \
            ['kvm', 'qemu']

        resp['data']['resources'][0]['data']['libvirt']['type'] = 'qemu'

    event_test(agent, 'libvirt/ping', post_func=post)


@if_libvirt
def test_preferred_libvirt_type():
    type = get_preferred_libvirt_type()
    assert type in ['qemu', 'kvm']
