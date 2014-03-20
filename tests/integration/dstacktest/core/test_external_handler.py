from dstack import ApiError
from common_fixtures import *  # NOQA

TEST_HANDLER_PREFIX = 'test-handler-'

def test_external_handler(admin_client):
    name = '{}-{}'.format(TEST_HANDLER_PREFIX, random_str())
    h = admin_client.create_external_handler(name=name,
                                             eventNames=['instance.start'])

    assert h.state == 'registering'
    assert h.get('eventNames') is None
    assert h.data.fields.eventNames == ['instance.start']

    h = wait_success(admin_client, h)

    assert h.state == 'active'
    assert h.data.fields.eventNames is None

    maps = h.externalHandlerExternalHandlerProcessMaps()
    assert len(maps) == 1
    assert maps[0].state == 'active'

    process = maps[0].externalHandlerProcess()
    assert process.state == 'active'
    assert process.name == 'instance.start'

    found = False
    for ep in admin_client.list_extension_point():
        if ep.name == 'instance.start':
            for impl in ep.implementations:
                if impl.name == name:
                    found = True

    assert found



#def test_external_handler_pre(admin_client):
#    name = 'handle-{}'.format(random_str())
#    h = admin_client.create_external_handler(name=name,
#                                             eventNames=['pre.garbase.start'])
#    h = wait_success(admin_client, h)
#
#    assert h.state == 'active'
#    assert len(h.externalHandlerProcesses()) == 1
#    assert len(h.externalHandlerProcesses()) == 1
#
#    maps = h.externalHandlerExternalHandlerProcessMaps()
#    assert len(maps) == 1
#    assert maps[0].state == 'active'
#
#    process = maps[0].externalHandlerProcess()
#    assert process.state == 'active'
#    assert process.name == 'garbase.start'
