import sys
import json
default_default_ip = ""
default_default_port = None
party_id = "10000"
role_default_ip = ""
role_default_port = None
role_serving_ip = ""
role_serving_port = None


def get_new_json(filepath):
    with open(filepath, 'rb') as f:
        global party_id
        json_data = json.load(f)
        data = json_data
        if default_default_ip:
            data['route_table']['default']['default'][0]['ip'] = default_default_ip
        if default_default_port:
            data['route_table']['default']['default'][0]['port'] = default_default_port
        role_default_conf = data['route_table']['10000']['default']
        if role_default_ip:
            role_default_conf[0]['ip'] = role_default_ip
        if role_default_port:
            role_default_conf[0]['port'] = role_default_port
        if party_id != "10000":
            del data['route_table']['10000']
            data['route_table'][party_id] = {}
            data['route_table'][party_id]['default'] = role_default_conf
        if role_serving_ip and role_serving_port:
            data['route_table'][party_id]["serving"] = [{
                'ip': role_serving_ip,
                'port': role_serving_port
            }]
    f.close()
    return json_data


def rewrite_json_file(filepath, json_data):
    with open(filepath, 'w') as f:
        json.dump(json_data, f, indent=4, separators=(',', ': '))
    f.close()


if __name__ == '__main__':
    json_path = sys.argv[1]
    m_json_data = get_new_json(json_path)
    rewrite_json_file(json_path, m_json_data)