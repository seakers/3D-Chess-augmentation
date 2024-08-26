#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Object models and methods for managing space system agencies.
"""

import json

from .util import Entity, EnumEntity

class AgencyType(EnumEntity):
    """Enumeration of recognized agency types.

    :var ACADEMIC: Academic agency.
    :vartype ACADEMIC: AgencyType
    :var GOVERNMENT: Government agency.
    :vartype GOVERNMENT: AgencyType
    :var COMMERCIAL: Commercial agency.
    :vartype COMMERCIAL: AgencyType
    """
    ACADEMIC = "ACADEMIC"
    GOVERNMENT = "GOVERNMENT"
    COMMERCIAL = "COMMERCIAL"

class Agency(Entity):
    """An organizational entity responsible for operating a space mission or asset.

    :param agencyType: Type of agency (default: None). Recognized values include: ACADEMIC, COMMERCIAL, GOVERNMENT.
    :type agencyType: str or AgencyType
    :param name: Full name of this entity (default: None).
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation (default: name).
    :type acronym: str
    :param _id: The unique identifier for this entity (default: None).
    :type _id: str
    """

    def __init__(self, agencyType=None, name=None, acronym=None, _id=None):
        self.name = name
        self.acronym = acronym if acronym else name
        self.agencyType = AgencyType.get(agencyType)
        super(Agency, self).__init__(_id, "Agency")

    @staticmethod
    def from_dict(d):
        """Parses an agency from a normalized JSON dictionary. Example:

        .. code-block:: json

           {
               "@id": "nasa",
               "@type": "Agency",
               "agencyType": "GOVERNMENT",
               "name": "National Aeronautics and Space Administration",
               "acronym": "NASA",
           }

        :param d: the JSON dictionary
        :type d: dict
        :returns: the agency
        :rtype: Agency
        """
        return Agency(
                    agencyType = d.get("agencyType", None),
                    name = d.get("name", None),
                    acronym = d.get("acronym", None),
                    _id = d.get("@id", None)
                )
