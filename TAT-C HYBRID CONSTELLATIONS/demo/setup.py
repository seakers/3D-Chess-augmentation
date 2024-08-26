from setuptools import setup, find_packages

setup(
    name='tatc',
    version='1.3.1',
    description='TAT-C Architecture Demo',
    author='Paul T. Grogan',
    author_email='pgrogan@stevens.edu',
    packages=find_packages(exclude=['test']),
    package_data={'tatc': ['resources/*.json']},
    include_package_data=True,
    scripts=['bin/arch_eval.py',
             'bin/cost_risk_proxy.py',
             'bin/gen_landsat8.py',
             'bin/instrument_proxy.py',
             'bin/launch_proxy.py',
             'bin/orbits_proxy.py',
             'bin/demo_tse.py',
             'bin/tsv.py',
             'bin/value_proxy.py'],
    install_requires=['isodate','numpy']
)
