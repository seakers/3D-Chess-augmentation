from setuptools import setup, find_packages

setup(
    name='tatc-tse',
    version='1.0',
    description='TAT-C Tradespace Search Executive',
    packages=find_packages(exclude=['test']),
    scripts=['bin/tse.py']
)
