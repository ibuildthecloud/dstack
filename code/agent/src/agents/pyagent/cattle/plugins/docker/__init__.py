import logging

from cattle import default_value, Config
from cattle.utils import memoize

log = logging.getLogger('docker')

_ENABLED = True


class DockerConfig:
    def __init__(self):
        pass

    @staticmethod
    def docker_enabled():
        return default_value('DOCKER_ENABLED', 'true') == 'true'

    @staticmethod
    def docker_host_ip():
        return default_value('DOCKER_HOST_IP', Config.agent_ip())

    @staticmethod
    def docker_home():
        return default_value('DOCKER_HOME', '/var/lib/docker')

    @staticmethod
    def docker_uuid_file():
        def_value = '{0}/.docker_uuid'.format(Config.home())
        return default_value('DOCKER_UUID_FILE', def_value)

    @staticmethod
    @memoize
    def docker_uuid():
        return Config.get_uuid_from_file('DOCKER_UUID',
                                         DockerConfig.docker_uuid_file())

    @staticmethod
    def url_base():
        return default_value('DOCKER_URL_BASE', None)

    @staticmethod
    def api_version():
        return default_value('DOCKER_API_VERSION', '1.8')


def docker_client(version=None):
    if version is None:
        version = DockerConfig.api_version()
    return Client(base_url=DockerConfig.url_base(),
                  version=version)


def pull_image(image, progress):
    _DOCKER_POOL.pull_image(image, progress)


def get_compute():
    return _DOCKER_COMPUTE

from .storage import DockerPool
from .compute import DockerCompute
from cattle import type_manager

try:
    from docker import Client
except:
    log.info('Disabling docker, docker-py not found')
    _ENABLED = False

try:
    if _ENABLED:
        docker_client().info()
except Exception, e:
    log.info('Disabling docker, could not contact docker')
    _ENABLED = False

if _ENABLED and DockerConfig.docker_enabled():
    _DOCKER_POOL = DockerPool()
    _DOCKER_COMPUTE = DockerCompute()
    type_manager.register_type(type_manager.STORAGE_DRIVER, _DOCKER_POOL)
    type_manager.register_type(type_manager.COMPUTE_DRIVER, _DOCKER_COMPUTE)
