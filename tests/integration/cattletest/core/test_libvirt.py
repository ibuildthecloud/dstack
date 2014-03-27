from xml.etree import ElementTree
from common_fixtures import *  # NOQA

import time
import os

EMPTY_IMAGE = 'https://googledrive.com/host/0BwwEzt2Cd9h1ZmNRWnlUWTdhcDg/' \
              'empty-10g.img.bz2'
EMPTY_IMAGE_CHECKSUM = '94a457b11d44fbcc6b5e12b31e29ca1b20db7c7d'
LIBVIRT_URI = 'ssh://root@localhost:22'
LIBVIRT_AGENT_UUID = os.getenv('LIBVIRT_TEST_AGENT_UUID', 'localhost_agent')

if_libvirt = pytest.mark.skipif('os.environ.get("LIBVIRT_TEST") != "true"',
                                reason='LIBVIRT_TEST is not set')


@pytest.fixture(scope='module')
def libvirt_context(admin_client):
    context = kind_context(admin_client, 'libvirt')

    image = create_type_by_uuid(admin_client, 'image', 'libvirt_test_image',
                                url=EMPTY_IMAGE,
                                checksum=EMPTY_IMAGE_CHECKSUM,
                                isPublic=True,
                                name='Libvirt Test Image',
                                kind='libvirt')

    context['imageId'] = image.id
    return context


@if_libvirt
def test_libvirt_create(client, admin_client, libvirt_context):
    image_id = libvirt_context['imageId']
    vm = client.create_virtual_machine(imageId=image_id)

    assert vm.state == 'creating'

    vm = wait_success(client, vm)

    assert vm.state == 'running'

    vm = admin_client.reload(vm)

    print vm.data.libvirt.xml

    xml = ElementTree.fromstring(vm.data.libvirt.xml)

    assert xml.findall('./memory')[0].text == str(64*1024)
    assert len(xml.findall('.//disk')) == 1

    vm = client.reload(vm)
    vm.stop(remove=True)


@if_libvirt
def test_libvirt_stop_start(client, libvirt_context):
    image_id = libvirt_context['imageId']
    vm = client.create_virtual_machine(imageId=image_id)
    vm = wait_success(client, vm)

    assert vm.state == 'running'

    for i in range(3):
        time.sleep(1)
        vm = vm.stop()
        assert vm.state == 'stopping'

        vm = wait_success(client, vm, timeout=30)
        assert vm.state == 'stopped'

        vm = vm.start()

        assert vm.state == 'starting'
        vm = wait_success(client, vm)
        assert vm.state == 'running'

    vm.stop(remove=True)
